package org.ogerardin.b2b.storage;

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
