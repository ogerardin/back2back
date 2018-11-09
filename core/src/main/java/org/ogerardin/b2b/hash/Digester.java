package org.ogerardin.b2b.hash;

import org.ogerardin.b2b.util.FormattingHelper;

/**
 * Provide a uniform interface to update a digest in association with a {@link DigestingInputStream}
 * and obtain a hash.
 */
public interface Digester {

    void putByte(byte b);

    void putBytes(byte[] b, int off, int result);

    byte[] hash();

    default String hexHash() {
        byte[] hashBytes = hash();
        return FormattingHelper.hex(hashBytes);
    }


}
