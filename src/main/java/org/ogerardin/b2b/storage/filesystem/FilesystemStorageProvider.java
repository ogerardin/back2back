package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.storage.StorageException;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FilesystemStorageProvider implements StorageService {

    final Path baseDirectory;

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

    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public InputStream getAsInputStream(String filename) throws FileNotFoundException {
        return null;
    }

    @Override
    public Resource getAsResource(String filename) throws FileNotFoundException {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void store(File file) throws IOException {

    }

    @Override
    public void store(Path path) throws IOException {

    }

    @Override
    public void store(InputStream inputStream, String canonicalPath) throws IOException {

    }
}
