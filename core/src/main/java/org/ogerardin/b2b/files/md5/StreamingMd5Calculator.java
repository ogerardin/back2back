package org.ogerardin.b2b.files.md5;

import org.ogerardin.b2b.util.FormattingHelper;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface StreamingMd5Calculator {

    byte[] md5Hash(InputStream inputStream) throws IOException;

    default String hexMd5Hash(InputStream inputStream) throws IOException {
        byte[] hashBytes = md5Hash(inputStream);
        inputStream.close();
        return FormattingHelper.hex(hashBytes);
    }

}
