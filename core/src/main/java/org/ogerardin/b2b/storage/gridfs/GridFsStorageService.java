package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.ogerardin.b2b.storage.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDbFactory;
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
    private final String bucket;

    public GridFsStorageService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, MongoTemplate mongoTemplate) {
        this(mongoDbFactory, mongoConverter, mongoTemplate, DEFAULT_BUCKET);
    }

    public GridFsStorageService(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, MongoTemplate mongoTemplate, String bucket) {
        this.gridFsTemplate = new GridFsTemplate(mongoDbFactory, mongoConverter, bucket);
        this.mongoTemplate = mongoTemplate;
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
        }
        catch (Exception e) {
            throw new StorageException("Failed to store file " + filename, e);
        }

    }

    @Override
    public Stream<Path> getAllPaths() {
        return gridFsTemplate.find(new Query()).stream()
                .map(GridFSFile::getFilename)
                .distinct()
                .map(GridFsStorageService::asPath);
    }

    @Override
    public Stream<FileVersion> getAllFileVersions() {
        return gridFsTemplate.find(new Query()).stream()
                .map(this::getFileVersion);
    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        GridFSDBFile fsdbFile = getGridFSDBFile(filename);
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
            store(new FileInputStream(file), file.getCanonicalPath());
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get InputStream for " + file, e);
        }
    }

    @Override
    public void store(Path path) {
        String canonicalPath = canonicalPath(path);
        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(path, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get InputStream for " + path, e);
        }
        store(inputStream, canonicalPath);
    }

    @Override
    public void store(InputStream inputStream, String filename) {
        gridFsTemplate.store(inputStream, filename, new Metadata());
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
    private GridFSDBFile getGridFSDBFile(String filename) throws StorageFileNotFoundException {
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
        FileVersion info = new FileVersion();
        info.setId(fsdbFile.getId().toString());
        info.setFilename(fsdbFile.getFilename());
        info.setSize(fsdbFile.getLength());
        info.setMd5hash(fsdbFile.getMD5());
        info.setStoredDate(fsdbFile.getUploadDate().toInstant());
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
        GridFSDBFile fsdbFile = getGridFSDBFile(filename);
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
}

