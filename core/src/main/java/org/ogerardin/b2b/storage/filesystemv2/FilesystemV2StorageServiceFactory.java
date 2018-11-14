package org.ogerardin.b2b.storage.filesystemv2;

import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.ogerardin.b2b.storage.filesystem.FilesystemStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FilesystemV2StorageServiceFactory implements StorageServiceFactory<FilesystemV2StorageService> {

    @Autowired
    FilesystemStorageProperties properties;

    public FilesystemV2StorageServiceFactory() {
    }

    @Override
    public FilesystemV2StorageService getStorageService(String id) {
        // The specified id is used as a subdirectory name under the configured base directory
        Path baseDirectory = properties.getBaseDirectory();
        Path directory = baseDirectory.resolve(id);
        return new FilesystemV2StorageService(directory);
    }
}
