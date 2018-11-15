package org.ogerardin.b2b.storage.filesystemv2;

import lombok.val;
import org.junit.Test;
import org.ogerardin.b2b.storage.StorageProviderTest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesystemStorageProviderV2Test extends StorageProviderTest<FilesystemStorageServiceV2> {

    private static final Path BASE_DIRECTORY = Paths.get("target/tmp-storage");

    public FilesystemStorageProviderV2Test() {
        val storageService = new FilesystemStorageServiceV2(BASE_DIRECTORY);
        storageService.init();
        storageService.deleteAll();
        setStorageService(storageService);
    }

    @Test
    public void testStoreAndRetrieve() throws Exception {
        super.testStoreAndRetrieve();
    }

    @Test
    public void testMultipleRevisions() throws Exception {
        super.testMultipleRevisions();
    }

}