package org.ogerardin.b2b.storage.filesystem;

import org.ogerardin.b2b.storage.StorageProviderTest;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemStorageProviderTest extends StorageProviderTest{

    private static final Path BASE_DIRECTORY = Paths.get("target/test-filesystem-storage-provider");

    public FileSystemStorageProviderTest() {
        super(new FilesystemStorageProvider(BASE_DIRECTORY));
    }

}