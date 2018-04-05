package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.ogerardin.b2b.storage.*;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link StorageService} using MongoDB's GridFS.
 *
 * This implementation allows the storage of several versions of the same file (= same path).
 *
 * Unless otherwise specified, methods that take a path as parameter refer to the latest stored version.
 */
public class GridFsStorageService implements StorageService {

    private static final String DEFAULT_BUCKET = "storage";

    private final GridFsTemplate gridFsTemplate;
    private final MongoTemplate mongoTemplate;
    private final MongoConverter mongoConverter;
    private final String bucket;

    public GridFsStorageService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, MongoTemplate mongoTemplate, MongoOperations mongoOperations) {
        this(mongoDbFactory, mongoConverter, mongoTemplate, DEFAULT_BUCKET);
    }

    public GridFsStorageService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, MongoTemplate mongoTemplate, String bucket) {
        this.gridFsTemplate = new GridFsTemplate(mongoDbFactory, mongoConverter, bucket);
        this.mongoTemplate = mongoTemplate;
        this.mongoConverter = mongoConverter;
        this.bucket = bucket;
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
        Stream<FileInfo> stream = gridFsTemplate.find(new Query()).stream()
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
            stream = stream.filter(fileInfo -> !fileInfo.isDeleted());
        }

        return stream;
    }

    private FileInfo asFileInfo(GridFSDBFile gridFSDBFile) {
        Path path = asPath(gridFSDBFile.getFilename());
        Metadata metadata = this.getMetadata(gridFSDBFile);
        return new FileInfo(path, metadata.isDeleted());
    }

    private Metadata getMetadata(GridFSDBFile f) {
        return mongoConverter.read(Metadata.class, f.getMetaData());
    }

    @Override
    public Stream<FileVersion> getAllFileVersions() {
        return gridFsTemplate.find(new Query()).stream()
                .map(this::getFileVersion);
    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        GridFSDBFile fsdbFile = getLatestGridFSDBFile(filename);
        return fsdbFile.getInputStream();

    }


    /**
     * An attempt to implement getGridFSDBFile using Mongo sorting, by querying the GrdiFS collection directly.
     * !!! UNRELIABLE !!!
     */
    private GridFSDBFile getGridFSDBFile_(String filename) throws StorageFileNotFoundException {
        // 1) perform a standard MongoTemplate query on the file bucket.
        Query query = new Query(GridFsCriteria.whereFilename().is(filename))
                .with(new Sort(Sort.Direction.DESC, "uploadDate"))
                .limit(1);
        List<BasicDBObject> gridFsFiles = mongoTemplate.find(query, BasicDBObject.class, bucket + ".files");
        if (gridFsFiles.isEmpty()) {
            throw new StorageFileNotFoundException(filename);
        }
        DBObject file = gridFsFiles.get(0);
        Object fileId = file.get("_id");

        // 2) perfom a gridFsTemplate query with the ID obtained previously. This returns a true GridFSDBFile
        GridFSDBFile fsdbFile = gridFsTemplate.findOne(
                new Query(GridFsCriteria.where("_id").is(fileId)));
        return fsdbFile;
    }

    /**
     * Returns all the {@link GridFSDBFile} corresponding to the specified file.
     */
    private List<GridFSDBFile> getGridFSDBFiles(String filename) {
        // we do the sorting in the stream pipeline as GridFsTemplate doesn't support sorted queries
        return gridFsTemplate.find(new Query(GridFsCriteria.whereFilename().is(filename)));
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
    public void store(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            store(inputStream, file.getCanonicalPath());
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + file, e);
        }
    }

    @Override
    public void store(Path path) {
        String canonicalPath = canonicalPath(path);
        try {
            InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ);
            store(inputStream, canonicalPath);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
    }

    @Override
    public void store(InputStream inputStream, String filename)  {
        try {
            gridFsTemplate.store(inputStream, filename, new Metadata());
            inputStream.close();
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store InputStream as " + filename, e);
        }
    }

    @Override
    public FileVersion[] getFileVersions(String filename) {
        List<GridFSDBFile> fsdbFiles = getGridFSDBFiles(filename);
        return fsdbFiles.stream()
                .map(this::getFileVersion)
                .toArray(FileVersion[]::new);
    }

    /**
     * Returns the {@link GridFSDBFile} corresponding to the most recent version of the specified file stored.
     */
    private GridFSDBFile getLatestGridFSDBFile(String filename) throws StorageFileNotFoundException {
        // we do the sorting in the stream pipeline as GridFs doesn't support sorted queries
        GridFSDBFile fsdbFile = gridFsTemplate.find(new Query(GridFsCriteria.whereFilename().is(filename))).stream()
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

    private FileVersion getFileVersion(GridFSDBFile fsdbFile) {
        FileVersion info = FileVersion.builder()
                .id(fsdbFile.getId().toString())
                .filename(fsdbFile.getFilename())
                .size(fsdbFile.getLength())
                .md5hash(fsdbFile.getMD5())
                .storedDate(fsdbFile.getUploadDate().toInstant())
                .build();
        return info;
    }

    @Override
    public FileVersion[] getFileVersions(Path path) {
        String canonicalPath = canonicalPath(path);
        return getFileVersions(canonicalPath);
    }

    @Override
    public FileVersion getLatestFileVersion(Path path) throws StorageFileNotFoundException {
        String canonicalPath = canonicalPath(path);
        return getLatestFileVersion(canonicalPath);
    }

    @Override
    public FileVersion getLatestFileVersion(String filename) throws StorageFileNotFoundException {
        GridFSDBFile fsdbFile = getLatestGridFSDBFile(filename);
        return getFileVersion(fsdbFile);
    }

    @Override
    public FileVersion getFileVersion(String versionId) throws StorageFileVersionNotFoundException {
        GridFSDBFile fsdbFile = getGridFSDBFileById(versionId);
        return getFileVersion(fsdbFile);
    }

    @Override
    public InputStream getFileVersionAsInputStream(String versionId) throws StorageFileVersionNotFoundException {
        GridFSDBFile fsdbFile = getGridFSDBFileById(versionId);
        return fsdbFile.getInputStream();
    }

    @Override
    public Resource getFileVersionAsResource(String versionId) throws StorageFileVersionNotFoundException {
        return new InputStreamResource(getFileVersionAsInputStream(versionId));
    }

    private GridFSDBFile getGridFSDBFileById(String versionId) throws StorageFileVersionNotFoundException {
        GridFSDBFile fsdbFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(versionId)));
        if (fsdbFile == null) {
            throw new StorageFileVersionNotFoundException(versionId);
        }
        return fsdbFile;
    }

    @Override
    public void untouchAll() {
        getFilesCollection()
                .updateMulti(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"metadata.deleted\": \"true\"}}"));
    }

    @Override
    public boolean touch(Path path) {
        String canonicalPath = canonicalPath(path);
        GridFSDBFile fsdbFile = null;
        try {
            fsdbFile = getLatestGridFSDBFile(canonicalPath);
            getFilesCollection()
                    .update(new BasicDBObject("_id", fsdbFile.getId()),
                            BasicDBObject.parse("{ \"$set\": {\"metadata.deleted\": \"false\"}}"));

            return true;
        } catch (StorageFileNotFoundException e) {
            return false;
        }
    }

    // this should be exposed by GridFsTemplate
    private DBCollection getFilesCollection() {
        return mongoTemplate.getCollection(this.bucket + ".files");
    }
}

