package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.ogerardin.b2b.storage.*;
import org.ogerardin.b2b.util.CipherHelper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
 * This implementation allows the storage of several versions of the same file (= same path).
 * TODO: move to using "revisions"
 *
 * Unless otherwise specified, methods that take a path as parameter refer to the latest stored version.
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

    private static Path asPath(String f) {
        return Paths.get(f);
    }

    @Override
    public void init() {
    }

    @Override
    public void store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("empty file " + file);
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            // This is a security check
            throw new StorageException("relative path outside current directory " + filename);
        }
        try {
            InputStream is = new BufferedInputStream(file.getInputStream());
            gridFsTemplate.store(is, filename);
            is.close();
        }
        catch (Exception e) {
            throw new StorageException("Failed to store file " + filename, e);
        }

    }


    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {

        Stream<FileInfo> stream = _allFiles()
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
    private Stream<GridFSFile> _allFiles() {
        List<GridFSFile> allFiles = new ArrayList<>();
        gridFsTemplate.find(new Query()).into(allFiles);
        return allFiles.stream();
    }

    private FileInfo asFileInfo(GridFSFile gridFSFile) {
        Path path = asPath(gridFSFile.getFilename());
        Metadata metadata = this.getMetadata(gridFSFile);
        return new FileInfo(path, metadata.isDeleted());
    }

    private Metadata getMetadata(GridFSFile f) {
        return mongoConverter.read(Metadata.class, f.getMetadata());
    }

    @Override
    public Stream<RevisionInfo> getAllRevisions() {
        return _allFiles()
                .map(this::getFileVersion);
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
    private List<GridFSFile> getGridFSFiles(String filename) {
        // we do the sorting in the stream pipeline as GridFsTemplate doesn't support sorted queries
        List<GridFSFile> allVersions = new ArrayList<>();
        gridFsTemplate.find(new Query(GridFsCriteria.whereFilename().is(filename))).into(allVersions);
        return allVersions;
    }

    @Override
    public Resource getAsResource(String filename) throws StorageFileNotFoundException {
        return new InputStreamResource(getAsInputStream(filename));
    }

    @Override
    public void deleteAll() {
        gridFsTemplate.delete(new Query());
    }

    @Override
    public String store(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return store(inputStream, file.getCanonicalPath());
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + file, e);
        }
    }

    @Override
    public String store(Path path) {
        String canonicalPath = canonicalPath(path);
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return store(inputStream, canonicalPath);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
    }

    @Override
    public String store(Path path, Key key) throws EncryptionException {
        String canonicalPath = canonicalPath(path);
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            return store(inputStream, canonicalPath, key);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
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
        List<GridFSFile> fsFiles = getGridFSFiles(filename);
        return fsFiles.stream()
                .map(this::getFileVersion)
                .toArray(RevisionInfo[]::new);
    }

    /**
     * Returns the {@link GridFSFile} corresponding to the most recent version of the specified file stored.
     */
    private GridFSFile getLatestGridFSFile(String filename) throws StorageFileNotFoundException {
        // we do the sorting in the stream pipeline as GridFs doesn't support sorted queries
        List<GridFSFile> allVersions = new ArrayList<>();
        gridFsTemplate.find(new Query(GridFsCriteria.whereFilename().is(filename))).into(allVersions);
        GridFSFile fsdbFile = allVersions.stream()
                .max(Comparator.comparing(GridFSFile::getUploadDate))
                .orElseThrow(() -> new StorageFileNotFoundException(filename));
        return fsdbFile;
    }

    private String canonicalPath(Path path) {
        String canonicalPath;
        try {
            canonicalPath = path.toFile().getCanonicalPath();
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get canonical path for " + path, e);
        }
        return canonicalPath;
    }

    private RevisionInfo getFileVersion(GridFSFile fsdbFile) {
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
    public RevisionInfo[] getRevisions(Path path) {
        String canonicalPath = canonicalPath(path);
        return getRevisions(canonicalPath);
    }

    @Override
    public RevisionInfo getLatestRevision(Path path) throws StorageFileNotFoundException {
        String canonicalPath = canonicalPath(path);
        return getLatestRevision(canonicalPath);
    }

    @Override
    public RevisionInfo getLatestRevision(String filename) throws StorageFileNotFoundException {
        GridFSFile fsFile = getLatestGridFSFile(filename);
        return getFileVersion(fsFile);
    }

    @Override
    public RevisionInfo getRevisionInfo(String versionId) throws StorageFileVersionNotFoundException {
        GridFSFile fsFile = getGridFSFileById(versionId);
        return getFileVersion(fsFile);
    }

    @Override
    public InputStream getRevisionAsInputStream(String versionId) throws StorageFileVersionNotFoundException {
        GridFSFile fsFile = getGridFSFileById(versionId);
        return getInputStream(fsFile);
    }

    @Override
    public InputStream getRevisionAsInputStream(String versionId, Key key) throws StorageFileVersionNotFoundException, EncryptionException {
        GridFSFile fsFile = getGridFSFileById(versionId);
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

    @Override
    public Resource getRevisionAsResource(String versionId) throws StorageFileVersionNotFoundException {
        return new InputStreamResource(getRevisionAsInputStream(versionId));
    }

    private GridFSFile getGridFSFileById(String versionId) throws StorageFileVersionNotFoundException {
        GridFSFile fsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(versionId)));
        if (fsFile == null) {
            throw new StorageFileVersionNotFoundException(versionId);
        }
        return fsFile;
    }

    @Override
    public void untouchAll() {
        getFilesCollection()
                .updateMany(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"metadata.deleted\": \"true\"}}"));
    }

    @Override
    public boolean touch(Path path) {
        String canonicalPath = canonicalPath(path);
        try {
            GridFSFile fsFile = getLatestGridFSFile(canonicalPath);
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

