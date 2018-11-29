package org.ogerardin.b2b.storage.filesystemv2;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.storage.*;
import org.ogerardin.b2b.storage.filesystem.FilesystemStorageService;
import org.ogerardin.b2b.util.CipherHelper;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link StorageService} using the filesystem, with support of multiple revisions.
 *
 * The mapping from remote path to local path is identical to {@link FilesystemStorageService}, with the addition of
 * a revision number to the file name; e.g. the revisions of a file named test.txt will be named text.txt#0, text.txt#1, etc.
 *
 * Implementation note: the revision ID is the path to the actual revision file.
 */
@Slf4j
public class FilesystemStorageServiceV2 extends FilesystemStorageService implements StorageService {

    public FilesystemStorageServiceV2(Path baseDirectory) {
        super(baseDirectory);
    }

    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {
        Stream<FileInfo> stream = getAllRevisions()
                .collect(Collectors.groupingBy(
                        RevisionInfo::getFilename,
                        Collectors.maxBy(Comparator.comparing(RevisionInfo::getStoredDate))))
                .values().stream()
                .map(Optional::get)
                .map(RevisionInfo::getFileInfo);

        // if required, keep only those that are not deleted
        if (!includeDeleted) {
            stream = stream.filter(FileInfo::isNotDeleted);
        }

        return stream;
    }

    protected Path localToRemote(Path localPath) {
        Path remoteBasePath = super.localToRemote(localPath);

        String basename = remoteBasePath.getFileName().toString();
        int sepIndex = basename.lastIndexOf("#");
        if (sepIndex < 0) {
            throw new StorageException("ill-formatted file name: " + basename);
        }

        String modifiedName = basename.substring(0, sepIndex);
        return remoteBasePath.resolveSibling(modifiedName);

    }

    @Override
    public String store(InputStream inputStream, String filename) {
        Path localPath = getLocalPathForNextRevision(Paths.get(filename));
        log.debug("Writing revision to {}", localPath);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(inputStream, localPath);
        } catch (IOException e) {
            throw new StorageException("Exception while copying input stream to local file " + localPath, e);
        }
        String revisionId = baseDirectory.relativize(localPath).toString();
        log.debug("Revision ID is {}", revisionId);
        return revisionId;

    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        RevisionInfo latestRevision = getLatestRevision(filename);
        Path localPath = getLocalPath(latestRevision);

        return getInputStream(localPath, filename);
    }

    @Override
    public void deleteAll() {
        try {
            //noinspection ResultOfMethodCallIgnored
            Files.walk(baseDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    //                .peek(System.out::println)
                    .forEach(java.io.File::delete);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to recursively delete " + baseDirectory, e);
        }
    }

    private Path getLocalPathForNextRevision(Path remotePath) {
        Path localBasePath = super.remoteToLocal(remotePath);
        String basename = localBasePath.getFileName().toString();
        int revNum = 0;
        while (true) {
            String revisionFileName = buildRevisionFileName(basename, revNum);
            Path revisionFilePath = localBasePath.resolveSibling(revisionFileName);
            if (!Files.exists(revisionFilePath)) {
                return revisionFilePath;
            }
            revNum++;
        }
    }

    @Override
    public RevisionInfo[] getRevisions(String filename) {
        Path remotePath = Paths.get(filename);
        log.debug("Getting all revisions for {}", remotePath);
        Path localBasePath = remoteToLocal(remotePath);
        log.debug("Local base path is {}", localBasePath);
        String basename = localBasePath.getFileName().toString();

        List<RevisionInfo> revisions = new ArrayList<>();
        int revNum = 0;
        while (true) {
            String revisionFileName = buildRevisionFileName(basename, revNum);
            Path revisionFilePath = localBasePath.resolveSibling(revisionFileName);
            log.debug("Trying {}", revisionFilePath);
            if (!Files.exists(revisionFilePath)) {
                log.debug("File does not exist - no more revisions");
                break;
            }
            RevisionInfo revisionInfo = buildRevisionInfo(remotePath, revisionFilePath);
            log.debug("revisionInfo = {}", revisionInfo);
            revisions.add(revisionInfo);

            revNum++;
        }

        return revisions.toArray(new RevisionInfo[0]);
    }

    @Override
    public RevisionInfo getLatestRevision(String filename) throws StorageFileNotFoundException {
        Path remotePath = Paths.get(filename);
        RevisionInfo revisionInfo = Arrays.stream(getRevisions(remotePath))
                .max(Comparator.comparing(RevisionInfo::getStoredDate))
                .orElseThrow(() -> new StorageFileNotFoundException(filename));
        return revisionInfo;
    }

    private String buildRevisionFileName(String basename, int revNum) {
        return String.format("%s#%d", basename, revNum);
    }

    private Path getLocalPath(RevisionInfo revision) {
        // for this StorageService, RevisionInfo.id holds the full local path of the corresponding revision
        return baseDirectory.resolve(revision.getId());
    }

    @Override
    protected RevisionInfo buildRevisionInfo(Path remotePath, Path localPath) {
        RevisionInfo revisionInfo = super.buildRevisionInfo(remotePath, localPath);
        // Store revision file path into RevisionInfo.id
        revisionInfo.setId(baseDirectory.relativize(localPath).toString());
        return revisionInfo;
    }

    @Override
    public RevisionInfo getRevisionInfo(String revisionId) {
        Path localPath = baseDirectory.resolve(revisionId);
        return buildRevisionInfo(localPath);
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId) throws IOException {
        RevisionInfo revisionInfo = getRevisionInfo(revisionId);
        Path localPath = getLocalPath(revisionInfo);
        return Files.newInputStream(localPath);
    }

    @Override
    public String store(InputStream inputStream, String filename, Key key) throws EncryptionException {
        Cipher aes = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        try (CipherInputStream cipherInputStream = new CipherInputStream(inputStream, aes)) {
            //TODO metadata to mark the file as encrypted?
            return store(cipherInputStream, filename);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to store CipherInputStream as " + filename, e);
        }
    }

    @Override
    public InputStream getAsInputStream(String filename, Key key) throws StorageFileNotFoundException, EncryptionException {
        //TODO check metadata to amake sure the file is encrypted?
        InputStream inputStream = getAsInputStream(filename);
        return getDecryptedInputStream(inputStream, key);
    }

    private InputStream getDecryptedInputStream(InputStream inputStream, Key key) throws EncryptionException {
        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.DECRYPT_MODE);
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        return cipherInputStream;
    }
}
