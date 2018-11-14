package org.ogerardin.b2b.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.util.stream.Stream;

/**
 * Interface for a service that provides storage and retrieval of files.
 * Each file is identified by its filename which is the original filename (including path) at upload time.
 * If supported by the implementation, specific revisions of a file are identified by an revisionId (a String).
 */
public interface StorageService {

    void init();

    /** List all known files */
    Stream<FileInfo> getAllFiles(boolean includeDeleted);

    /** List all known revisions of all known files */
    Stream<RevisionInfo> getAllRevisions();

    /** Obtain an {@link java.io.InputStream} to read the contents of the latest revision of the specified file */
    InputStream getAsInputStream(String filename) throws StorageFileNotFoundException;

    default InputStream getAsInputStream(Path path) throws StorageFileNotFoundException {
        String canonicalPath = canonicalPath(path);
        return getAsInputStream(canonicalPath);
    }

    /** Obtain an {@link java.io.InputStream} to read the unencrypted contents of the latest version of the specified file,
     * using the specified key to decrypt it. */
    InputStream getAsInputStream(String filename, Key key) throws StorageFileNotFoundException, EncryptionException;

    default Resource getAsResource(String filename) throws StorageFileNotFoundException {
        return new InputStreamResource(getAsInputStream(filename));
    }

    default String store(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return store(inputStream, file.getCanonicalPath());
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + file, e);
        }
    }

    default String store(Path path) {
        String canonicalPath = canonicalPath(path);
        try (InputStream inputStream = Files.newInputStream(path)) {
            return store(inputStream, canonicalPath);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
    }

    default String store(Path path, Key key) throws EncryptionException {
        String canonicalPath = canonicalPath(path);
        try (InputStream inputStream = Files.newInputStream(path)) {
            return store(inputStream, canonicalPath, key);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
    }

    /** Store the specified {@link java.io.InputStream} associated to the specified filename */
    String store(InputStream inputStream, String filename);

    /** Store the specified {@link java.io.InputStream} contents in its encrypted form using the specified key,
     *  associated to the specified filename */
    String store(InputStream inputStream, String filename, Key key) throws EncryptionException;

    /** Deletes all the stored files and versions */
    void deleteAll();

    /** Lists all the known versions of the specified file, passed as a String */
    RevisionInfo[] getRevisions(String filename);

    default RevisionInfo[] getRevisions(Path path) {
        String canonicalPath = canonicalPath(path);
        return getRevisions(canonicalPath);
    }

    default String canonicalPath(Path path) {
        String canonicalPath;
        try {
            canonicalPath = path.toFile().getCanonicalPath();
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get canonical path for " + path, e);
        }
        return canonicalPath;
    }

    /** Returns information about the latest stored version of the specified file, passed as a String */
    RevisionInfo getLatestRevision(String filename) throws StorageFileNotFoundException;

    default RevisionInfo getLatestRevision(Path path) throws StorageFileNotFoundException {
        String canonicalPath = canonicalPath(path);
        return getLatestRevision(canonicalPath);
    }

    /** Returns information about a file version, designated by its ID */
    RevisionInfo getRevisionInfo(String revisionId) throws StorageFileVersionNotFoundException;

    /** Returns an {@link java.io.InputStream} to read the contents of a file version, designated by its ID */
    InputStream getRevisionAsInputStream(String revisionId) throws StorageFileVersionNotFoundException, IOException;

    /** Returns an {@link java.io.InputStream} to read the contents of a file version, designated by its ID,
     * in its unencrypted form using the specified key */
    InputStream getRevisionAsInputStream(String revisionId, Key key) throws StorageFileVersionNotFoundException, EncryptionException;

    default Resource getRevisionAsResource(String revisionId) throws StorageFileVersionNotFoundException, IOException {
        return new InputStreamResource(getRevisionAsInputStream(revisionId));
    }

    /** Mark all files as "untouched" (= potentially deleted) */
    void untouchAll();

    /** Mark the specified file as "touched" (= not deleted) */
    boolean touch(Path path);

    long countDeleted();
}
