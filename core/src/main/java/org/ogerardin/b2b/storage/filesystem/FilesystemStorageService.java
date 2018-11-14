package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.hash.md5.ByteArrayMD5Calculator;
import org.ogerardin.b2b.storage.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Key;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.ogerardin.b2b.util.LambdaExceptionUtil.rethrowFunction;

/**
 * Implementation of {@link StorageService} using the filesystem.
 * All stored files are saved under a base directory; the path of the local file is the path of the original file,
 * re-rooted at the base directory; for example if saving file /x/y/z and the base directory is /a/b/c, then the file
 * will be saved as /a/b/c/x/y/z.
 *
 * This implemetation does not manage multiple revisions of a stored file, nor deleted files.
 */
public class FilesystemStorageService implements StorageService {

    @Autowired
    protected ByteArrayMD5Calculator md5Calculator;

    protected final Path baseDirectory;

    public FilesystemStorageService(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(baseDirectory);
        } catch (IOException e) {
            throw new StorageException("failed to create base directory: " + baseDirectory, e);
        }
        if (! Files.isWritable(baseDirectory)) {
            throw new StorageException("Base directory is not writable: " + baseDirectory);
        }
    }

    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {
        //FIXME includeDeleted is ignored
        try {
            return Files.walk(baseDirectory)
                    .filter(p -> !Files.isDirectory(p))
                    .map(this::localToRemote)
                    .map(path -> new FileInfo(path, false));
        } catch (IOException e) {
            throw new StorageException("Exception while listing local files", e);
        }
    }

    @Override
    public Stream<RevisionInfo> getAllRevisions() {
        try {
            return Files.walk(baseDirectory)
                    .filter(p -> !Files.isDirectory(p))
                    .map(rethrowFunction(this::buildRevisionInfo));
        } catch (Exception e) {
            throw new StorageException("Exception while listing local files", e);
        }
    }

    protected Path remoteToLocal(Path remotePath) {
        // turn into a relative path, e.g. C:\xxxx\yyy will become xxx\yyy
        Path root = remotePath.getRoot();
        Path relativePath = (root != null) ? root.relativize(remotePath) : remotePath;

        // local path is relative to the directory
        return baseDirectory.resolve(relativePath);
    }

    /**
     * Returns the remote path (external) corresponding to the specified local path.
     * The remote path is the local path relative to the storage base directory.
     */
    protected Path localToRemote(Path localPath) {
        // the remote file path is relative to the base directory
        Path relativePath = baseDirectory.relativize(localPath);
        // to rebuilt the original absolute remote file, we need a root Path
        //FIXME this will only work on single-root filesystems
        Path root = baseDirectory.getFileSystem().getRootDirectories().iterator().next();
        return root.resolve(relativePath);
    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);

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
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to recursively delete " + baseDirectory, e);
        }
    }

    @Override
    public String store(InputStream inputStream, String filename) {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(inputStream, localPath);
        } catch (IOException e) {
            throw new StorageException("Exception while copying input stream to local file " + localPath, e);
        }
        return null;
    }

    @Override
    public RevisionInfo[] getRevisions(String filename) {
        try {
            RevisionInfo revisionInfo = getLatestRevision(filename);
            return new RevisionInfo[]{ revisionInfo };
        }
        catch (StorageFileNotFoundException e) {
            return new RevisionInfo[0];
        }
    }


    @Override
    public RevisionInfo getLatestRevision(String filename) throws StorageFileNotFoundException {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);
        if (! Files.exists(localPath)) {
            throw new StorageFileNotFoundException(remotePath.toString());
        }
        RevisionInfo fileInfo = buildRevisionInfo(remotePath, localPath);
        return fileInfo;
    }

    @Override
    public RevisionInfo getRevisionInfo(String revisionId) throws StorageFileNotFoundException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId) throws IOException, StorageFileNotFoundException {
        throw new RuntimeException("Not implemented");
    }

    protected RevisionInfo buildRevisionInfo(Path remotePath, Path localPath) {
        BasicFileAttributes fileAttributes;
        try {
            BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(localPath, BasicFileAttributeView.class);
            fileAttributes = fileAttributeView.readAttributes();
        } catch (IOException e) {
            throw new StorageException("Failed to get file attributes for file " + localPath, e);
        }

        String md5hash = null;
        if (md5Calculator != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(localPath);
                md5hash = md5Calculator.hexMd5Hash(fileBytes);
            } catch (IOException e) {
                throw new StorageException("Failed to read file " + localPath, e);
            }
        }

        RevisionInfo info = RevisionInfo.builder()
                .id(null) //no ID for FilesystemStorageService
                .filename(remotePath.toString())
                .size(fileAttributes.size())
                .storedDate(fileAttributes.creationTime().toInstant())
                .md5hash(md5hash)
                .build();
        return info;
    }

    protected RevisionInfo buildRevisionInfo(Path localPath) {
        return buildRevisionInfo(localToRemote(localPath), localPath);
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
        return 0;
    }

    @Override
    public String store(InputStream inputStream, String filename, Key key) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId, Key key) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String store(Path path, Key key) throws EncryptionException {
        throw new RuntimeException("not implemented");
    }

    @Override
    public InputStream getAsInputStream(String filename, Key key) throws StorageFileNotFoundException, EncryptionException {
        throw new RuntimeException("not implemented");
    }
}
