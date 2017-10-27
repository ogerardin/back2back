package org.ogerardin.b2b.storage.filesystem;

import org.junit.Test;
import org.ogerardin.b2b.storage.StorageProviderTest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemStorageProviderTest extends StorageProviderTest<FilesystemStorageService> {

    private static final Path BASE_DIRECTORY = Paths.get("target/test-filesystem-storage-provider");

    public FileSystemStorageProviderTest() {
        FilesystemStorageService storageService = new FilesystemStorageService(BASE_DIRECTORY);
        setStorageService(storageService);
    }

    @Test
    public void testLoadAll() throws Exception {
        super.testLoadAll(storageService);
    }
}