package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.ogerardin.b2b.storage.*;
import org.ogerardin.b2b.util.CipherHelper;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.util.StreamUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link StorageService} using MongoDB's GridFS.
 *
 * This implementation allows the storage of several revisions of the same file (= same path).
 * Unless otherwise specified, methods that take a path as parameter refer to the latest revision.
 */
public class GridFsStorageService implements StorageService {

    private static final String DEFAULT_BUCKET = "storage";

    private final GridFsTemplate gridFsTemplate;
    private final MongoTemplate mongoTemplate;
    private final MongoConverter mongoConverter;
    private final String bucketName;

    public GridFsStorageService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, MongoTemplate mongoTemplate, MongoOperations mongoOperations) {
        this(mongoDbFactory, mongoConverter, mongoTemplate, DEFAULT_BUCKET);
    }

    public GridFsStorageService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, MongoTemplate mongoTemplate, String bucketName) {
        this.gridFsTemplate = new GridFsTemplate(mongoDbFactory, mongoConverter, bucketName);
        this.mongoTemplate = mongoTemplate;
        this.mongoConverter = mongoConverter;
        this.bucketName = bucketName;
    }

    @Override
    public void init() {
    }


    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {

        Stream<FileInfo> stream = getAllGridFSFiles()
                // group by filename and map to the latest GridFSFile
                .collect(Collectors.groupingBy(
                        GridFSFile::getFilename,
                        Collectors.maxBy(Comparator.comparing(GridFSFile::getUploadDate))))
                // map values (=latest GridFSFile) to FileInfo
                .values().stream()
                .map(Optional::get)
                .map(this::asFileInfo);

        // if required, keep only those that are not deleted
        if (!includeDeleted) {
            stream = stream.filter(FileInfo::isNotDeleted);
        }

        return stream;
    }

    // returns a Stream of all GridFSFiles in the collection
    private Stream<GridFSFile> getAllGridFSFiles() {
        GridFSFindIterable fsFindIterable = gridFsTemplate.find(new Query());
        Stream<GridFSFile> stream = StreamUtils.createStreamFromIterator(fsFindIterable.iterator());
        return stream;
    }

    protected FileInfo asFileInfo(GridFSFile gridFSFile) {
        Path path = Paths.get(gridFSFile.getFilename());
        Metadata metadata = this.getMetadata(gridFSFile);
        return new FileInfo(path, metadata.isDeleted());
    }

    private Metadata getMetadata(GridFSFile f) {
        return mongoConverter.read(Metadata.class, f.getMetadata());
    }

    @Override
    public Stream<RevisionInfo> getAllRevisions() {
        return getAllGridFSFiles()
                .map(this::buildRevisionInfo);
    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        GridFSFile fsdbFile = getLatestGridFSFile(filename);
        return getInputStream(fsdbFile);
    }


    @Override
    public InputStream getAsInputStream(String filename, Key key) throws StorageFileNotFoundException, EncryptionException {
        GridFSFile fsFile = getLatestGridFSFile(filename);
        return getDecryptedInputStream(fsFile, key);
    }

    /**
     * An attempt to implement getGridFSDBFile using Mongo sorting, by querying the GridFS collection directly.
     * !!! UNRELIABLE !!!
     */
    private GridFSFile getGridFSDBFile_(String filename) throws StorageFileNotFoundException {
        // 1) perform a standard MongoTemplate query on the file bucket.
        Query query = new Query(GridFsCriteria.whereFilename().is(filename))
                .with(new Sort(Sort.Direction.DESC, "uploadDate"))
                .limit(1);
        List<BasicDBObject> gridFsFiles = mongoTemplate.find(query, BasicDBObject.class, bucketName + ".files");
        if (gridFsFiles.isEmpty()) {
            throw new StorageFileNotFoundException(filename);
        }
        DBObject file = gridFsFiles.get(0);
        Object fileId = file.get("_id");

        // 2) perfom a gridFsTemplate query with the ID obtained previously. This returns a true GridFSDBFile
        GridFSFile fsdbFile = gridFsTemplate.findOne(
                new Query(GridFsCriteria.where("_id").is(fileId)));
        return fsdbFile;
    }

    /**
     * Returns all the {@link GridFSFile} corresponding to the specified file.
     */
    private Stream<GridFSFile> getGridFSFiles(String filename) {
        GridFSFindIterable fsFindIterable = gridFsTemplate.find(new Query(GridFsCriteria.whereFilename().is(filename)));
        Stream<GridFSFile> stream = StreamUtils.createStreamFromIterator(fsFindIterable.iterator());
        return stream;
    }


    @Override
    public void deleteAll() {
        gridFsTemplate.delete(new Query());
    }

    @Override
    public String store(InputStream inputStream, String filename)  {
        ObjectId objectId = gridFsTemplate.store(inputStream, filename, new Metadata());
        return objectId.toString();
    }

    @Override
    public String store(InputStream inputStream, String filename, Key key) throws EncryptionException {
        Cipher aes = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        try {
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, aes);
            Metadata metadata = new Metadata();
            metadata.setEncrypted(true);
            ObjectId objectId = gridFsTemplate.store(cipherInputStream, filename, metadata);
            cipherInputStream.close();
            return objectId.toString();
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store CipherInputStream as " + filename, e);
        }

    }

    @Override
    public RevisionInfo[] getRevisions(String filename) {
        return getGridFSFiles(filename)
                .map(this::buildRevisionInfo)
                .toArray(RevisionInfo[]::new);
    }

    /**
     * Returns the {@link GridFSFile} corresponding to the most recent version of the specified file stored.
     */
    private GridFSFile getLatestGridFSFile(String filename) throws StorageFileNotFoundException {
        // we do the sorting in the stream pipeline as GridFS doesn't support sorted queries
        List<GridFSFile> revisions = new ArrayList<>();
        gridFsTemplate.find(new Query(GridFsCriteria.whereFilename().is(filename))).into(revisions);
        GridFSFile fsFile = revisions.stream()
                .max(Comparator.comparing(GridFSFile::getUploadDate))
                .orElseThrow(() -> new StorageFileNotFoundException(filename));
        return fsFile;
    }

    private RevisionInfo buildRevisionInfo(GridFSFile fsdbFile) {
        RevisionInfo info = RevisionInfo.builder()
                .id(fsdbFile.getObjectId().toString())
                .filename(fsdbFile.getFilename())
                .size(fsdbFile.getLength())
                .md5hash(fsdbFile.getMD5())
                .storedDate(fsdbFile.getUploadDate().toInstant())
                .build();
        return info;
    }

    @Override
    public RevisionInfo getLatestRevision(String filename) throws StorageFileNotFoundException {
        GridFSFile fsFile = getLatestGridFSFile(filename);
        return buildRevisionInfo(fsFile);
    }

    @Override
    public RevisionInfo getRevisionInfo(String revisionId) throws StorageFileVersionNotFoundException {
        GridFSFile fsFile = getGridFSFileById(revisionId);
        return buildRevisionInfo(fsFile);
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId) throws StorageFileVersionNotFoundException {
        GridFSFile fsFile = getGridFSFileById(revisionId);
        return getInputStream(fsFile);
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId, Key key) throws StorageFileVersionNotFoundException, EncryptionException {
        GridFSFile fsFile = getGridFSFileById(revisionId);
        return getDecryptedInputStream(fsFile, key);
    }

    private InputStream getInputStream(GridFSFile fsdbFile) {
        ObjectId objectId = fsdbFile.getObjectId();
        return getGridFs().openDownloadStream(objectId);
    }

    private GridFSBucket getGridFs() {
        MongoDatabase db = mongoTemplate.getDb();
        return GridFSBuckets.create(db, bucketName);
    }

    private InputStream getDecryptedInputStream(GridFSFile fsFile, Key key) throws EncryptionException {
        Metadata metadata = getMetadata(fsFile);
        if (! metadata.isEncrypted()) {
            throw new EncryptionException("File is not encrypted");
        }

        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.DECRYPT_MODE);
        InputStream inputStream = getInputStream(fsFile);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        return cipherInputStream;
    }

    private GridFSFile getGridFSFileById(String revisionId) throws StorageFileVersionNotFoundException {
        GridFSFile fsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(revisionId)));
        if (fsFile == null) {
            throw new StorageFileVersionNotFoundException(revisionId);
        }
        return fsFile;
    }

    @Override
    public void untouchAll() {
        getFilesCollection()
                .updateMany(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"metadata.deleted\": \"true\"}}"));
    }

    @Override
    public boolean touch(String filename) {
        try {
            GridFSFile fsFile = getLatestGridFSFile(filename);
            getFilesCollection()
                    .updateOne(new BasicDBObject("_id", fsFile.getId()),
                            BasicDBObject.parse("{ \"$set\": {\"metadata.deleted\": \"false\"}}"));

            return true;
        } catch (StorageFileNotFoundException e) {
            return false;
        }
    }

    @Override
    public long countDeleted() {
        return getAllFiles(true)
                .filter(FileInfo::isDeleted)
                .count();
    }

    // this should be exposed by GridFsTemplate
    private MongoCollection<Document> getFilesCollection() {
        return mongoTemplate.getCollection(this.bucketName + ".files");
    }
}

