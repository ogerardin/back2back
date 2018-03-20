package org.ogerardin.b2b.files.md5;

import org.ogerardin.b2b.util.FormattingHelper;

@FunctionalInterface
public interface MD5Calculator {

    byte[] md5Hash(byte[] bytes);

    default String hexMd5Hash(byte[] bytes) {
        byte[] hashBytes = md5Hash(bytes);
        return FormattingHelper.hex(hashBytes);
    }

}
