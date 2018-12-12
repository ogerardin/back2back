package org.ogerardin.b2b.storage;

/**
 * Indicates that the requested version of a file was not found in the {@link StorageService}
 */
public class RevisionNotFoundException extends Exception {

    public RevisionNotFoundException(String s) {
        super(s);
    }

    public RevisionNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RevisionNotFoundException(Throwable throwable) {
        super(throwable);
    }

}
