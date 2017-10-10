package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.storage.StorageException;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class FilesystemStorageProvider implements StorageService {

    private final Path baseDirectory;

    public FilesystemStorageProvider(Path baseDirectory) {
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
    public Stream<Path> loadAll() {
        try {
            return Files.walk(baseDirectory)
                    .filter(p -> !Files.isDirectory(p))
                    .map(this::localToRemote);
        } catch (IOException e) {
            throw new StorageException("Exception while listing local files", e);
        }
    }

    private Path remoteToLocal(Path path) {
        // turn into a relative path, e.g. C:\xxxx\yyy will become xxx\yyy
        Path root = path.getRoot();
        Path relativePath = (root != null) ? root.relativize(path) : path;

        // local path is relative to the baseDirectory
        return baseDirectory.resolve(relativePath);
    }

    private Path localToRemote(Path path) {
        return baseDirectory.relativize(path);
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
    public void store(InputStream inputStream, String originalFilename) {
        Path remotePath = Paths.get(originalFilename);
        Path localPath = remoteToLocal(remotePath);
        try {
            Files.createDirectories(localPath.getParent());
            Files.copy(inputStream, localPath);
        } catch (IOException e) {
            throw new StorageException("Exception while copying input stream to local file " + localPath, e);
        }

    }

}
