package org.ogerardin.b2b.storage.filesystem;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;
import org.ogerardin.b2b.hash.ByteArrayHashCalculator;
import org.ogerardin.b2b.storage.*;
import org.ogerardin.b2b.util.CipherHelper;
import org.ogerardin.b2b.util.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Key;
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
    protected ByteArrayHashCalculator hashCalculator;

    protected final Path baseDirectory;

    private static final Escaper ROOT_ESCAPER = new PercentEscaper("", true);

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
        if (includeDeleted) {
            throw new NotImplementedException("includeDeleted not supported");
        }
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

    /**
     *  Returns the path for a file that can be used to store the contents of the speficied remote path.
     *  If the specified file is absolute, it is converted to a relative path
    */
    protected Path remoteToLocal(Path remotePath) {
        Path root = remotePath.getRoot();
        Path relativePath = (root != null) ? root.relativize(remotePath) : remotePath;

        return baseDirectory.resolve(relativePath);
    }

    /**
     * Returns the remote path (external) corresponding to the specified local path.
     * The remote path is the local path relative to the storage base directory.
     */
    protected Path localToRemote(Path localPath) {
        // the remote file path is relative to the base directory
        Path relativePath = baseDirectory.relativize(localPath);
        // check if the first path component is an escaped root, which means the remote file is absolute
        //FIXME this will only work if the localPath and the remotePath have the same filesystem
        String pathComponent0 = relativePath.getName(0).toString();
        for (Path root : baseDirectory.getFileSystem().getRootDirectories()) {
            String rootName = root.toString();
            String escapedRoot = ROOT_ESCAPER.escape(rootName);
            if (escapedRoot.equals(pathComponent0)) {
                Path subpath = relativePath.subpath(1, relativePath.getNameCount());
                return root.resolve(subpath);
            }
        }
        // no root matches the first path component, assume the file is relative
        return relativePath;
    }

    @Override
    public InputStream getAsInputStream(String filename) throws FileNotFoundException {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);

        return getInputStream(localPath, filename);
    }

    protected static InputStream getInputStream(Path localPath, String filename) throws FileNotFoundException {
        try {
            return Files.newInputStream(localPath);
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException(e);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get InputStream for " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            FileSystemUtils.deleteRecursively(baseDirectory);
//            MoreFiles.deleteRecursively(baseDirectory) ;
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
        catch (FileNotFoundException e) {
            return new RevisionInfo[0];
        }
    }


    @Override
    public RevisionInfo getLatestRevision(String filename) throws FileNotFoundException {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);
        if (! Files.exists(localPath)) {
            throw new FileNotFoundException(remotePath.toString());
        }
        RevisionInfo fileInfo = buildRevisionInfo(remotePath, localPath);
        return fileInfo;
    }

    @Override
    public RevisionInfo getRevisionInfo(String revisionId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId) throws IOException {
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

/*
        String md5hash = null;
        if (hashCalculator != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(localPath);
                md5hash = hashCalculator.hexHash(fileBytes);
            } catch (IOException e) {
                throw new StorageException("Failed to read file " + localPath, e);
            }
        }
*/

        RevisionInfo info = RevisionInfo.builder()
                .id(null) //no ID for FilesystemStorageService
                .filename(remotePath.toString())
                .size(fileAttributes.size())
                .storedDate(fileAttributes.creationTime().toInstant())
//                .md5hash(md5hash)
                .build();
        return info;
    }

    protected RevisionInfo buildRevisionInfo(Path localPath) {
        return buildRevisionInfo(localToRemote(localPath), localPath);
    }

    @Override
    public void delete(String filename) {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);
        try {
            Files.delete(localPath);
        } catch (IOException e) {
            throw new StorageException("Failed to delete " + localPath, e);
        }
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId, Key key) {
        throw new RuntimeException("not implemented");
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
    public InputStream getAsInputStream(String filename, Key key) throws FileNotFoundException, EncryptionException {
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
