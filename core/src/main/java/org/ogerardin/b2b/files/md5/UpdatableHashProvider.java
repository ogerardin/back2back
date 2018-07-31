package org.ogerardin.b2b.files.md5;

/**
 * An {@link Updatable} that also provides a {@link #hash()} method. Intended to compute hashes.
 */
public interface UpdatableHashProvider<T> extends Updatable<T>, HashProvider {

}
