package org.ogerardin.b2b.storage.filesystem;

import org.junit.Test;
import org.ogerardin.b2b.storage.StorageProviderTest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemStorageProviderTest extends StorageProviderTest{

    private static final Path BASE_DIRECTORY = Paths.get("target/test-filesystem-storage-provider");

    private FilesystemStorageProvider storageService;

    public FileSystemStorageProviderTest() {
        setStorageService(new FilesystemStorageProvider(BASE_DIRECTORY));
    }

    public void setStorageService(FilesystemStorageProvider storageService) {
        this.storageService = storageService;
    }

    @Test
    public void testLoadAll() throws Exception {
        super.testLoadAll(storageService);
    }
}