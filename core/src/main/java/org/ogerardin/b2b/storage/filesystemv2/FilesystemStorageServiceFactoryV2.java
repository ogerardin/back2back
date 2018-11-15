package org.ogerardin.b2b.storage.filesystemv2;

import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.ogerardin.b2b.storage.filesystem.FilesystemStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class FilesystemStorageServiceFactoryV2 implements StorageServiceFactory<FilesystemStorageServiceV2> {

    @Autowired
    FilesystemStorageProperties properties;

    public FilesystemStorageServiceFactoryV2() {
    }

    @Override
    public FilesystemStorageServiceV2 getStorageService(String id) {
        // The specified id is used as a subdirectory name under the configured base directory
        Path baseDirectory = properties.getBaseDirectory();
        Path directory = baseDirectory.resolve(id);
        return new FilesystemStorageServiceV2(directory);
    }
}
