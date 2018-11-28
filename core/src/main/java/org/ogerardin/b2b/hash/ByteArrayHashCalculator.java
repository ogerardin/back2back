package org.ogerardin.b2b.hash;

import org.ogerardin.b2b.util.FormattingHelper;

/**
 * Classes that implement this interface provide a way to compute a MD5 hash from a byte array.
 */
@FunctionalInterface
public interface ByteArrayHashCalculator {

    byte[] hash(byte[] bytes);

    default String hexHash(byte[] bytes) {
        return FormattingHelper.hex(hash(bytes));
    }

}
