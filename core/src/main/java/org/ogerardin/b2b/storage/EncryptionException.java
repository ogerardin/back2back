package org.ogerardin.b2b.storage;

public class EncryptionException extends Exception {
    public EncryptionException() {
    }

    public EncryptionException(String s) {
        super(s);
    }

    public EncryptionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public EncryptionException(Throwable throwable) {
        super(throwable);
    }

    public EncryptionException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }

}
