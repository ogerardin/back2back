package org.ogerardin.b2b.storage.filesystemv2;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.storage.*;
import org.ogerardin.b2b.storage.filesystem.FilesystemStorageService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link StorageService} using the filesystem.
 * The mapping from remote path to local path is identical to {@link FilesystemStorageService}, but this
 * class manages file revisions by adding a revision number to the file name; e.g. the first revision of a file
 * named test.txt will be saved as text.txt#0, the second as text.txt#1, etc.
 */
@Slf4j
public class FilesystemV2StorageService extends FilesystemStorageService implements StorageService {

    public FilesystemV2StorageService(Path baseDirectory) {
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
        log.debug("Writing new revision to {}", localPath);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(inputStream, localPath);
        } catch (IOException e) {
            throw new StorageException("Exception while copying input stream to local file " + localPath, e);
        }
        return localPath.toString();

    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        RevisionInfo latestRevision = getLatestRevision(filename);
        Path localPath = getLocalPath(latestRevision);

        try {
            return Files.newInputStream(localPath);
        } catch (FileNotFoundException e) {
            throw new StorageFileNotFoundException(e);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get InputStream for " + filename, e);
        }
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
        log.debug("Local path is {}", localBasePath);
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
                .orElseThrow(() -> new StorageFileNotFoundException(remotePath.toString()));
        return revisionInfo;
    }

    private String buildRevisionFileName(String basename, int revNum) {
        return String.format("%s#%d", basename, revNum);
    }

    private Path getLocalPath(RevisionInfo revision) {
        // for this StorageService, RevisionInfo.id holds the full local path of the corresponding revision
        return Paths.get(revision.getId());
    }

    @Override
    protected RevisionInfo buildRevisionInfo(Path remotePath, Path localPath) {
        RevisionInfo revisionInfo = super.buildRevisionInfo(remotePath, localPath);
        // Store revision file path into RevisionInfo.id
        revisionInfo.setId(localPath.toString());
        return revisionInfo;
    }

    @Override
    public RevisionInfo getRevisionInfo(String revisionId) {
        Path localPath = Paths.get(revisionId);
        return buildRevisionInfo(localPath);
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId) throws IOException {
        RevisionInfo revisionInfo = getRevisionInfo(revisionId);
        Path localPath = getLocalPath(revisionInfo);
        return Files.newInputStream(localPath);
    }

    @Override
    public void untouchAll() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean touch(Path path) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public long countDeleted() {
        throw new RuntimeException("not implemented");
    }

}
