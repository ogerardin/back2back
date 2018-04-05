package org.ogerardin.b2b.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Interface for a service that provides storage and retrieval of files.
 * Each file is identified by its filename which is the original filename (including path) at upload time.
 * If supported by the implementation, specific versions of a file are identified by an versionId (a String).
 */
public interface StorageService {

    void init();

    Stream<FileInfo> getAllFiles(boolean includeDeleted);

    Stream<FileVersion> getAllFileVersions();
    InputStream getAsInputStream(String filename) throws StorageFileNotFoundException;
    Resource getAsResource(String filename) throws StorageFileNotFoundException;

    void store(MultipartFile file);
    void store(File file);
    void store(Path path);
    void store(InputStream inputStream, String filename);

    void deleteAll();

    FileVersion[] getFileVersions(String filename);
    FileVersion[] getFileVersions(Path path);

    FileVersion getLatestFileVersion(Path path) throws StorageFileNotFoundException;
    FileVersion getLatestFileVersion(String filename) throws StorageFileNotFoundException;

    FileVersion getFileVersion(String versionId) throws StorageFileVersionNotFoundException;
    InputStream getFileVersionAsInputStream(String versionId) throws StorageFileVersionNotFoundException;
    Resource getFileVersionAsResource(String versionId) throws StorageFileVersionNotFoundException;

    void untouchAll();
    boolean touch(Path path);
}
