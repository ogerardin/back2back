package org.ogerardin.b2b.storage;

/**
 * Indicates that the requested version of a file was not found in the {@link StorageService}
 */
public class StorageFileRevisionNotFoundException extends Exception {

    public StorageFileRevisionNotFoundException(String s) {
        super(s);
    }

    public StorageFileRevisionNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public StorageFileRevisionNotFoundException(Throwable throwable) {
        super(throwable);
    }

}
