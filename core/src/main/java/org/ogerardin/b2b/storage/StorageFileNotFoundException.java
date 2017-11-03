package org.ogerardin.b2b.storage;

public class StorageFileNotFoundException extends Exception {

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageFileNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
