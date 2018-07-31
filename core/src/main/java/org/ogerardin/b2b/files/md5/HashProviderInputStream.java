package org.ogerardin.b2b.files.md5;

import java.io.InputStream;

/**
 * A subclass of {@link UpdaterInputStream} that is intended to compute hashes.
 * It updates an {@link UpdatableHashProvider} object and provides a convenient {@link #hash()} method.
 */
public class HashProviderInputStream extends UpdaterInputStream<UpdatableHashProvider> implements HashProvider {

    public HashProviderInputStream(InputStream stream, UpdatableHashProvider updatable) {
        super(stream, updatable);
    }

    @Override
    public byte[] hash() {
        return getUpdatable().hash();
    }
}
