package org.ogerardin.b2b.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Interface for a service that provides storage and retrieval of files.
 * Each file is identified by its filename which is the supposed to reflect the original filename (including path) at
 * upload time.
 */
public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<Path> getAllPaths();

    InputStream getAsInputStream(String filename) throws StorageFileNotFoundException;

    Resource getAsResource(String filename) throws StorageFileNotFoundException;

    void deleteAll();

    void store(File file);

    void store(Path path);

    void store(InputStream inputStream, String filename);

    StoredFileInfo query(String filename) throws StorageFileNotFoundException;

    StoredFileInfo query(Path path) throws StorageFileNotFoundException;

}
