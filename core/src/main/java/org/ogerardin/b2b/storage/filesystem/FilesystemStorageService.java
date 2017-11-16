package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.files.MD5Calculator;
import org.ogerardin.b2b.storage.StorageException;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StoredFileInfo;
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

/**
 * Implementation of {@link StorageService} using the filesystem. All stored files are saved under a base directory; the
 * path of the local file is the path of the original file, re-rooted at the base directory; e.g. if saving file
 * /x/y/z and the {@link FilesystemStorageService}'s base directory is /a/b/c, then the file will be saved as
 * /a/b/c/x/y/z.
 */
public class FilesystemStorageService implements StorageService {

    @Autowired
    MD5Calculator md5Calculator;

    private final Path directory;

    public FilesystemStorageService(Path directory) {
        this.directory = directory;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new StorageException("failed to create base directory: " + directory, e);
        }
        if (! Files.isWritable(directory)) {
            throw new StorageException("Base directory is not writable: " + directory);
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
    public Stream<Path> getAllPaths() {
        try {
            return Files.walk(directory)
                    .filter(p -> !Files.isDirectory(p))
                    .map(this::localToRemote);
        } catch (IOException e) {
            throw new StorageException("Exception while listing local files", e);
        }
    }

    @Override
    public Stream<StoredFileInfo> getAllStoredFileInfos() {
        try {
            return Files.walk(directory)
                    .filter(p -> !Files.isDirectory(p))
                    .map(this::getStoredFileInfo);
        } catch (IOException e) {
            throw new StorageException("Exception while listing local files", e);
        }
    }

    private Path remoteToLocal(Path path) {
        // turn into a relative path, e.g. C:\xxxx\yyy will become xxx\yyy
        Path root = path.getRoot();
        Path relativePath = (root != null) ? root.relativize(path) : path;

        // local path is relative to the directory
        return directory.resolve(relativePath);
    }

    private Path localToRemote(Path path) {
        return directory.relativize(path);
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
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
    //                .peek(System.out::println)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to recursively delete " + directory, e);
        }
    }

    @Override
    public void store(File file) {
        Path path = Paths.get(file.toURI());
        store(path);
    }

    @Override
    public void store(Path path) {
        Path localPath = remoteToLocal(path);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(path, localPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Exception while trying to copy file " + path + " to " + localPath, e);
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
    public StoredFileInfo query(String filename) {
        Path remotePath = Paths.get(filename);
        return query(remotePath);
    }

    @Override
    public StoredFileInfo query(Path remotePath) {
        Path localPath = remoteToLocal(remotePath);

        return getStoredFileInfo(remotePath, localPath);
    }

    private StoredFileInfo getStoredFileInfo(Path remotePath, Path localPath) {
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

        StoredFileInfo info = new StoredFileInfo();
        info.setId(null); //no ID for FilesystemStorageService
        info.setFilename(remotePath.toString());
        info.setSize(fileAttributes.size());
        info.setStoredDate(fileAttributes.creationTime().toInstant());
        info.setMd5hash(md5hash);
        return info;
    }

    private StoredFileInfo getStoredFileInfo(Path localPath) {
        return getStoredFileInfo(localToRemote(localPath), localPath);
    }
}
