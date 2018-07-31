package org.ogerardin.b2b.files.md5;

import org.ogerardin.b2b.util.FormattingHelper;

/**
 * A {@link HashProvider} is simply a class that provides a {@link #hash()} method.
 */
public interface HashProvider {

    byte[] hash();

    default String hexHash() {
        byte[] hashBytes = hash();
        return FormattingHelper.hex(hashBytes);
    }

}
