package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FilesystemStorageServiceFactory implements StorageServiceFactory<FilesystemStorageService> {

    @Autowired
    FilesystemStorageProperties properties;

    public FilesystemStorageServiceFactory() {
    }

    @Override
    public FilesystemStorageService getStorageService(String name) {
        Path baseDirectory = properties.getBaseDirectory();
        Path directory = baseDirectory.resolve(name);
        return new FilesystemStorageService(directory);
    }
}
