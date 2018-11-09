package org.ogerardin.b2b.hash.md5;

import org.ogerardin.b2b.util.FormattingHelper;

/**
 * Classes that implement this interface provide a way to compute a MD5 hash from a byte array.
 */
@FunctionalInterface
public interface ByteArrayMD5Calculator {

    byte[] md5Hash(byte[] bytes);

    default String hexMd5Hash(byte[] bytes) {
        byte[] hashBytes = md5Hash(bytes);
        return FormattingHelper.hex(hashBytes);
    }

}
