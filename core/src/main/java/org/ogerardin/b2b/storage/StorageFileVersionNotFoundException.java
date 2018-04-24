package org.ogerardin.b2b.storage;

/**
 * Indicates that the requested version of a file was not found in the {@link StorageService}
 */
public class StorageFileVersionNotFoundException extends Exception {

    public StorageFileVersionNotFoundException(String s) {
        super(s);
    }

    public StorageFileVersionNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public StorageFileVersionNotFoundException(Throwable throwable) {
        super(throwable);
    }

}
