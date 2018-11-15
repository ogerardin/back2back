package org.ogerardin.b2b.storage.filesystem;

import org.junit.Test;
import org.ogerardin.b2b.storage.StorageProviderTest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesystemStorageProviderTest extends StorageProviderTest<FilesystemStorageService> {

    private static final Path BASE_DIRECTORY = Paths.get("target/tmp-storage");

    public FilesystemStorageProviderTest() {
        FilesystemStorageService storageService = new FilesystemStorageService(BASE_DIRECTORY);
        storageService.init();
        storageService.deleteAll();
        setStorageService(storageService);
    }

    @Test
    public void testStoreAndRetrieve() throws Exception {
        super.testStoreAndRetrieve();
    }
}