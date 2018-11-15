package org.ogerardin.b2b.storage.filesystem;

import lombok.val;
import org.junit.Test;
import org.ogerardin.b2b.storage.StorageProviderTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesystemStorageProviderTest extends StorageProviderTest<FilesystemStorageService> {

    private static final Path BASE_DIRECTORY;
    static {
        try {
            BASE_DIRECTORY = Files.createTempDirectory(FilesystemStorageProviderTest.class.getSimpleName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FilesystemStorageProviderTest() {
        val storageService = new FilesystemStorageService(BASE_DIRECTORY);
        storageService.init();
        storageService.deleteAll();
        setStorageService(storageService);
    }

    @Test
    public void testStoreAndRetrieve() throws Exception {
        super.testStoreAndRetrieve();
    }
}