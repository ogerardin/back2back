package org.ogerardin.b2b.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<Path> loadAll();

    InputStream getAsInputStream(String filename) throws StorageFileNotFoundException;

    Resource getAsResource(String filename) throws StorageFileNotFoundException;

    void deleteAll();

    void store(File file);

    void store(Path path);

    void store(InputStream inputStream, String filename);

    StoredFileInfo query(String filename) throws StorageFileNotFoundException;

    StoredFileInfo query(Path path) throws StorageFileNotFoundException;

}
