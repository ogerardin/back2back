package org.ogerardin.b2b.storage;

import lombok.val;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
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
 * Unless otherwise specified, methods that take a filename as parameter refer to the latest revision.
 */
public interface StorageService {

    void init();

    /** List all known files */
    Stream<FileInfo> getAllFiles(boolean includeDeleted);

    /** List all known revisions of all known files */
    Stream<RevisionInfo> getAllRevisions();

    /** Obtain an {@link java.io.InputStream} to read the contents of the latest revision of the specified file */
    InputStream getAsInputStream(String filename) throws FileNotFoundException;

    default InputStream getAsInputStream(Path path) throws FileNotFoundException {
        return getAsInputStream(normalizedPath(path));
    }

    /** Obtain an {@link java.io.InputStream} to read the unencrypted contents of the latest version of the specified file,
     * using the specified key to decrypt it. */
    InputStream getAsInputStream(String filename, Key key) throws FileNotFoundException, EncryptionException;

    default Resource getAsResource(String filename) throws FileNotFoundException {
        return new InputStreamResource(getAsInputStream(filename));
    }

    default String store(File file) {
        return store(file.toPath());
    }

    default String store(Path path) {
        String normalizedPath = normalizedPath(path);
        try (val inputStream = Files.newInputStream(path)) {
            return store(inputStream, normalizedPath);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
    }

    default String store(Path path, Key key) throws EncryptionException {
        String normalizedPath = normalizedPath(path);
        try (val inputStream = Files.newInputStream(path)) {
            return store(inputStream, normalizedPath, key);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store " + path, e);
        }
    }

    /**
     * Store the specified {@link java.io.InputStream} associated to the specified filename
     * @return the revision ID of the newly created revision, or null if revisions not supported
     */
    String store(InputStream inputStream, String filename);

    /**
     * Store the specified {@link java.io.InputStream} contents in its encrypted form using the specified key,
     *  associated to the specified filename
     * @return the revision ID of the newly created revision, or null if revisions not supported
     */
    String store(InputStream inputStream, String filename, Key key) throws EncryptionException;

    /** Deletes all the stored files and versions */
    void deleteAll();

    /** Lists all the known versions of the specified file, passed as a String */
    RevisionInfo[] getRevisions(String filename);

    default RevisionInfo[] getRevisions(Path path) {
        String normalizedPath = normalizedPath(path);
        return getRevisions(normalizedPath);
    }

    default String normalizedPath(Path path) {
        return path.normalize().toString();
    }

    /** Returns information about the latest stored version of the specified file, passed as a String */
    RevisionInfo getLatestRevision(String filename) throws FileNotFoundException;

    default RevisionInfo getLatestRevision(Path path) throws FileNotFoundException {
        String normalizedPath = normalizedPath(path);
        return getLatestRevision(normalizedPath);
    }

    /** Returns information about a file version, designated by its ID */
    RevisionInfo getRevisionInfo(String revisionId) throws RevisionNotFoundException;

    /** Returns an {@link java.io.InputStream} to read the contents of a file version, designated by its ID */
    InputStream getRevisionAsInputStream(String revisionId) throws RevisionNotFoundException, IOException;

    /** Returns an {@link java.io.InputStream} to read the contents of a file version, designated by its ID,
     * in its unencrypted form using the specified key */
    InputStream getRevisionAsInputStream(String revisionId, Key key) throws RevisionNotFoundException, EncryptionException;

    default Resource getRevisionAsResource(String revisionId) throws RevisionNotFoundException, IOException {
        return new InputStreamResource(getRevisionAsInputStream(revisionId));
    }

    /** Mark the specified file as deleted */
    void delete(String filename);

    default void delete(Path path) {
        String normalizedPath = normalizedPath(path);
        delete(normalizedPath);
    }

}
