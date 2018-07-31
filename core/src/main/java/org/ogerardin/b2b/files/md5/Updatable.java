package org.ogerardin.b2b.files.md5;

public interface Updatable<T> {

    void putByte(byte b);

    void putBytes(byte[] b, int off, int result);

    T get();
}
