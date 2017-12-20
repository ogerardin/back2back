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
    Stream<StoredFileInfo> getAllStoredFileInfos();

    InputStream getAsInputStream(String filename) throws StorageFileNotFoundException;
    Resource getAsResource(String filename) throws StorageFileNotFoundException;

    void deleteAll();

    void store(File file);
    void store(Path path);
    void store(InputStream inputStream, String filename);

    StoredFileInfo[] getStoredFileInfos(String filename);
    StoredFileInfo[] getStoredFileInfos(Path path);

    StoredFileInfo getStoredFileInfo(Path path) throws StorageFileNotFoundException;
    StoredFileInfo getStoredFileInfo(String filename) throws StorageFileNotFoundException;
    StoredFileInfo getStoredFileInfoById(String itemId);
}
