package org.ogerardin.b2b.files.md5;

/**
 * Provide a uniform interface to update an embedded object of type T in association with a {@link UpdaterInputStream}.
 * @param <T> the type of the embedded object that is updated.
 */
public interface Updatable<T> {

    void putByte(byte b);

    void putBytes(byte[] b, int off, int result);

    T get();
}
