package org.ogerardin.b2b.files.md5.guava;

import com.google.common.hash.Hashing;

import java.io.InputStream;

@SuppressWarnings("UnstableApiUsage")
public class GuavaMd5DigestInputStream extends GuavaDigestInputStream {

    @SuppressWarnings("deprecation")
    public GuavaMd5DigestInputStream(InputStream stream) {
        super(stream, Hashing.md5());
    }

}
