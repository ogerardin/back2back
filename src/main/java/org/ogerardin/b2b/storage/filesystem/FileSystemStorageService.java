package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.storage.StorageException;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("empty file " + file);
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            // This is a security check
            throw new IllegalArgumentException("relative path outside current directory " + filename);
        }
        try {
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public InputStream getAsInputStream(String filename) throws FileNotFoundException {
        return new FileInputStream(rootLocation.resolve(filename).toFile());
    }

    @Override
    public Resource getAsResource(String filename) throws FileNotFoundException {
        InputStream is = getAsInputStream(filename);
        return new InputStreamResource(is);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void store(File file) throws IOException {
        Files.copy(new FileInputStream(file), this.rootLocation.resolve(file.getCanonicalPath()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void store(Path path) throws IOException {
        Files.copy(path, this.rootLocation.resolve(path), StandardCopyOption.REPLACE_EXISTING);

    }

    @Override
    public void store(InputStream inputStream, String canonicalPath) throws IOException {
        Files.copy(inputStream, this.rootLocation.resolve(canonicalPath), StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
