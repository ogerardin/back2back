package org.ogerardin.b2b.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.Key;
import java.util.stream.Stream;

/**
 * Interface for a service that provides storage and retrieval of files.
 * Each file is identified by its filename which is the original filename (including path) at upload time.
 * If supported by the implementation, specific versions of a file are identified by an versionId (a String).
 */
public interface StorageService {

    void init();

    /** List all known files */
    Stream<FileInfo> getAllFiles(boolean includeDeleted);

    /** List all known versions of all known files */
    Stream<FileVersion> getAllFileVersions();

    /** Obtain an {@link java.io.InputStream} to read the contents of the latest version of the specified file */
    InputStream getAsInputStream(String filename) throws StorageFileNotFoundException;

    /** Obtain an {@link java.io.InputStream} to read the unencrypted contents of the latest version of the specified file,
     * using the specified key to decrypt it. */
    InputStream getAsInputStream(String filename, Key key) throws StorageFileNotFoundException, EncryptionException;

    /** Obtain a {@link Resource} that corresponds to the latest version of the specified file*/
    Resource getAsResource(String filename) throws StorageFileNotFoundException;

    /** Store the file referenced by the specified {@link org.springframework.web.multipart.MultipartFile} */
    void store(MultipartFile file);

    /** Store the file designated by the specified {@link java.io.File} */
    String store(File file);

    /** Store the file designated by the specified {@link Path} */
    String store(Path path);

    /** Store the file designated by the specified {@link Path} in its encrypted form using the specified key */
    String store(Path path, Key key) throws EncryptionException;

    /** Store the specified {@link java.io.InputStream} associated to the specified filename */
    String store(InputStream inputStream, String filename);

    /** Store the specified {@link java.io.InputStream} contents in its encrypted form using the specified key,
     *  associated to the specified filename */
    String store(InputStream inputStream, String filename, Key key) throws EncryptionException;

    /** Deletes all the stored files and versions */
    void deleteAll();

    /** Lists all the known versions of the specified file, passed as a String */
    FileVersion[] getFileVersions(String filename);

    /** Lists all the known versions of the specified file, passed as a {@link Path} */
    FileVersion[] getFileVersions(Path path);

    /** Returns information about the latest stored version of the specified file, passed as a {@link Path} */
    FileVersion getLatestFileVersion(Path path) throws StorageFileNotFoundException;

    /** Returns information about the latest stored version of the specified file, passed as a String */
    FileVersion getLatestFileVersion(String filename) throws StorageFileNotFoundException;

    /** Returns information about a file version, designated by its ID */
    FileVersion getFileVersion(String versionId) throws StorageFileVersionNotFoundException;

    /** Returns an {@link java.io.InputStream} to read the contents of a file version, designated by its ID */
    InputStream getFileVersionAsInputStream(String versionId) throws StorageFileVersionNotFoundException;

    /** Returns an {@link java.io.InputStream} to read the contents of a file version, designated by its ID,
     * in its unencrypted form using the specified key */
    InputStream getFileVersionAsInputStream(String versionId, Key key) throws StorageFileVersionNotFoundException, EncryptionException;

    /** Returns a {@link Resource that corresponds to the file version, designated by the specified ID */
    Resource getFileVersionAsResource(String versionId) throws StorageFileVersionNotFoundException;

    /** Mark all files as "untouched" (potentially deleted) */
    void untouchAll();

    /** Mark the specified file as "touched" (not deleted) */
    boolean touch(Path path);
}
