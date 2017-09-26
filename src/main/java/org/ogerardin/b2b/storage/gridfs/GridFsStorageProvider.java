package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.ogerardin.b2b.storage.StorageException;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

@Service
@Configuration
@EnableConfigurationProperties({GridFsStorageProperties.class})
public class GridFsStorageProvider implements StorageService {

    private final GridFsStorageProperties properties;
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public GridFsStorageProvider(GridFsStorageProperties properties, MongoDbFactory mongoDbFactory, MongoConverter mongoConverter) {
        this.properties = properties;
        this.gridFsTemplate = new GridFsTemplate(mongoDbFactory, mongoConverter, properties.getBucket());
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
    public InputStream getAsInputStream(String filename) throws FileNotFoundException {
        GridFSDBFile fsdbFile = gridFsTemplate.findOne(new Query(GridFsCriteria.whereFilename().is(filename)));
        if (fsdbFile == null) {
            throw new FileNotFoundException(filename);
        }
        return fsdbFile.getInputStream();

    }

    @Override
    public Resource getAsResource(String filename) throws FileNotFoundException {
        return new InputStreamResource(getAsInputStream(filename));
    }

    @Override
    public void deleteAll() {
        gridFsTemplate.delete(new Query());
    }

    @Override
    public void store(File file) throws IOException {
        store(new FileInputStream(file), file.getCanonicalPath());
    }

    @Override
    public void store(Path path) throws IOException {
        store(Files.newInputStream(path, StandardOpenOption.READ), path.toFile().getCanonicalPath());
    }

    @Override
    public void store(InputStream inputStream, String canonicalPath) {
        gridFsTemplate.store(inputStream, canonicalPath);
    }
}
