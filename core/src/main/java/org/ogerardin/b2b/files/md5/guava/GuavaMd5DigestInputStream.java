package org.ogerardin.b2b.files.md5.guava;

import com.google.common.hash.Hashing;
import org.ogerardin.b2b.files.md5.HashProvider;

import java.io.InputStream;

public class GuavaMd5DigestInputStream extends GuavaDigestInputStream implements HashProvider {

    public GuavaMd5DigestInputStream(InputStream stream) {
        //noinspection deprecation
        super(stream, Hashing.md5());
    }

    @Override
    public byte[] hash() {
        return getUpdatable().get().hash().asBytes();
    }}
