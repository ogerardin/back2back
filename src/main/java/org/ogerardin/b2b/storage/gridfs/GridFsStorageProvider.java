package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.ogerardin.b2b.storage.StorageException;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StoredFileInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
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
import java.util.stream.Stream;

public class GridFsStorageProvider implements StorageService {

    public static final String DEFAULT_BUCKET = "storage";

    private final GridFsTemplate gridFsTemplate;

    public GridFsStorageProvider(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter) {
        this(mongoDbFactory, mongoConverter, DEFAULT_BUCKET);
    }

    public GridFsStorageProvider(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter, String bucket) {
        this.gridFsTemplate = new GridFsTemplate(mongoDbFactory, mongoConverter, bucket);
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
    public Stream<Path> loadAll() {
        return gridFsTemplate.find(new Query()).stream()
                .map(GridFSFile::getFilename)
                .map(f -> Paths.get(f));
    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        GridFSDBFile fsdbFile = getGridFSDBFile(filename);
        return fsdbFile.getInputStream();

    }

    private GridFSDBFile getGridFSDBFile(String filename) throws StorageFileNotFoundException {
        GridFSDBFile fsdbFile = gridFsTemplate.findOne(new Query(GridFsCriteria.whereFilename().is(filename)));
        if (fsdbFile == null) {
            throw new StorageFileNotFoundException(filename);
        }
        return fsdbFile;
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

    private String canonicalPath(Path path) {
        String canonicalPath;
        try {
            canonicalPath = path.toFile().getCanonicalPath();
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get canonical path for " + path, e);
        }
        return canonicalPath;
    }

    @Override
    public void store(InputStream inputStream, String filename) {
        gridFsTemplate.store(inputStream, filename);
    }

    @Override
    public StoredFileInfo query(String filename) throws StorageFileNotFoundException {
        GridFSDBFile fsdbFile = getGridFSDBFile(filename);
        StoredFileInfo info = new StoredFileInfo();
        info.setFilename(fsdbFile.getFilename());
        info.setSize(fsdbFile.getLength());
        info.setMd5hash(fsdbFile.getMD5());
        info.setStoredDate(fsdbFile.getUploadDate().toInstant());
        return info;
    }

    @Override
    public StoredFileInfo query(Path path) throws StorageFileNotFoundException {
        String canonicalPath = canonicalPath(path);
        return query(canonicalPath);
    }
}
