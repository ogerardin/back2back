package org.ogerardin.b2b.storage;

/**
 * Indicates that a requested file is unknown to the {@link StorageService}
 */
public class FileNotFoundException extends Exception {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
