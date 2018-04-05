package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.ogerardin.b2b.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.ogerardin.b2b.util.LambdaExceptionUtil.rethrowFunction;

/**
 * Implementation of {@link StorageService} using the filesystem.
 * All stored files are saved under a base directory; the path of the local file is the path of the original file,
 * re-rooted at the base directory; for example if saving file /x/y/z and the base directory is /a/b/c, then the file
 * will be saved as /a/b/c/x/y/z.
 *
 * This implemetation does not manage multiple versions of a stored file, nor deleted files.
 */
public class FilesystemStorageService implements StorageService {

    @Autowired
    MD5Calculator md5Calculator;

    private final Path baseDirectory;

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
    public void store(MultipartFile file) {
        // get the original filename (which might be relative or absolute)
        String originalFilename = file.getOriginalFilename();
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new StorageException("Exception while get input stream from " + file, e);
        }
        store(inputStream, originalFilename);
    }

    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {
        //FIXME consider includeDeleted
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
    public Stream<FileVersion> getAllFileVersions() {
        try {
            return Files.walk(baseDirectory)
                    .filter(p -> !Files.isDirectory(p))
                    .map(rethrowFunction(this::_getFileVersion));
        } catch (Exception e) {
            throw new StorageException("Exception while listing local files", e);
        }
    }

    private Path remoteToLocal(Path remotePath) {
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
    private Path localToRemote(Path localPath) {
        return baseDirectory.relativize(localPath);
    }

    @Override
    public InputStream getAsInputStream(String filename) throws StorageFileNotFoundException {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);

        try {
            return Files.newInputStream(localPath, StandardOpenOption.READ);
        } catch (FileNotFoundException e) {
            throw new StorageFileNotFoundException(e);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to get InputStream for " + filename, e);
        }
    }

    @Override
    public Resource getAsResource(String filename) throws StorageFileNotFoundException {
        return new InputStreamResource(getAsInputStream(filename));
    }

    @Override
    public void deleteAll() {
        //noinspection ResultOfMethodCallIgnored
        try {
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
    public void store(File file) {
        Path path = Paths.get(file.toURI());
        store(path);
    }

    @Override
    public void store(Path remotePath) {
        Path localPath = remoteToLocal(remotePath);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(remotePath, localPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to copy file " + remotePath + " to " + localPath, e);
        }
    }

    @Override
    public void store(InputStream inputStream, String filename) {
        Path remotePath = Paths.get(filename);
        Path localPath = remoteToLocal(remotePath);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(inputStream, localPath);
        } catch (IOException e) {
            throw new StorageException("Exception while copying input stream to local file " + localPath, e);
        }

    }

    @Override
    public FileVersion[] getFileVersions(String filename) {
        Path path = Paths.get(filename);
        return getFileVersions(path);
    }

    @Override
    public FileVersion[] getFileVersions(Path remotePath) {
        try {
            FileVersion fileInfo = getLatestFileVersion(remotePath);
            return new FileVersion[]{ fileInfo };
        }
        catch (StorageFileNotFoundException e) {
            return new FileVersion[0];
        }
    }

    @Override
    public FileVersion getLatestFileVersion(Path path) throws StorageFileNotFoundException {
        Path localPath = remoteToLocal(path);
        FileVersion fileInfo = _getFileVersion(path, localPath);
        return fileInfo;
    }

    @Override
    public FileVersion getLatestFileVersion(String filename) throws StorageFileNotFoundException {
        Path remotePath = Paths.get(filename);
        return getLatestFileVersion(remotePath);
    }

    @Override
    public FileVersion getFileVersion(String versionId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public InputStream getFileVersionAsInputStream(String versionId) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public Resource getFileVersionAsResource(String versionId) {
        throw new RuntimeException("Not implemented");
    }

    private FileVersion _getFileVersion(Path remotePath, Path localPath) throws StorageFileNotFoundException {
        BasicFileAttributes fileAttributes;
        try {
            BasicFileAttributeView fileAttributeView = Files.getFileAttributeView(localPath, BasicFileAttributeView.class);
            fileAttributes = fileAttributeView.readAttributes();
        } catch (FileNotFoundException e) {
            throw new StorageFileNotFoundException(e);
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

        FileVersion info = FileVersion.builder()
                .id(null) //no ID for FilesystemStorageService
                .filename(remotePath.toString())
                .size(fileAttributes.size())
                .storedDate(fileAttributes.creationTime().toInstant())
                .md5hash(md5hash)
                .build();
        return info;
    }

    private FileVersion _getFileVersion(Path localPath) throws StorageFileNotFoundException {
        return _getFileVersion(localToRemote(localPath), localPath);
    }

    @Override
    public void untouchAll() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean touch(Path path) {
        throw new RuntimeException("not implemented");
    }
}
