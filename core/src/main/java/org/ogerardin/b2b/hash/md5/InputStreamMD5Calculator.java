package org.ogerardin.b2b.hash.md5;

import org.ogerardin.b2b.util.FormattingHelper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classes that implement this interface provide a way to compute a MD5 hash from an InputStream
 */
@FunctionalInterface
public interface InputStreamMD5Calculator {

    byte[] md5Hash(InputStream inputStream) throws IOException;

    default String hexMd5Hash(InputStream inputStream) throws IOException {
        byte[] hashBytes = md5Hash(inputStream);
        inputStream.close();
        return FormattingHelper.hex(hashBytes);
    }

}