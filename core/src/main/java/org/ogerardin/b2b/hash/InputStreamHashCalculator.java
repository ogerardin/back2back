package org.ogerardin.b2b.hash;

import org.ogerardin.b2b.util.FormattingHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classes that implement this interface provide a way to compute a MD5 hash from an InputStream
 */
@FunctionalInterface
public interface InputStreamHashCalculator {

    byte[] hash(InputStream inputStream) throws IOException;

    default String hexHash(InputStream inputStream) throws IOException {
        byte[] hashBytes = hash(inputStream);
        inputStream.close();
        return FormattingHelper.hex(hashBytes);
    }

}
