package org.ogerardin.b2b.hash.md5.guava;

import com.google.common.hash.Hashing;

import java.io.InputStream;

@SuppressWarnings("UnstableApiUsage")
class GuavaMd5DigestInputStream extends GuavaDigestInputStream {

    @SuppressWarnings("deprecation")
    GuavaMd5DigestInputStream(InputStream stream) {
        super(stream, Hashing.md5());
    }

}
