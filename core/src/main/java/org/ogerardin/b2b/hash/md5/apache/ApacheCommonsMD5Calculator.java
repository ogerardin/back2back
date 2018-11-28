package org.ogerardin.b2b.hash.md5.apache;

import org.apache.commons.codec.digest.DigestUtils;
import org.ogerardin.b2b.hash.DigestingInputStream;
import org.ogerardin.b2b.hash.HashProvider;
import org.ogerardin.b2b.hash.md5.java.JavaDigestInputStream;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;


/**
 * MD5 hash calculator using Apache commons codec.
 * Most performant on average.
 */
@Component
public class ApacheCommonsMD5Calculator implements HashProvider {

    @Override
    public String name() {
        return "MD5";
    }

    @Override
    public byte[] hash(byte[] bytes) {
        return DigestUtils.md5(bytes);
    }

    @Override
    public byte[] hash(InputStream inputStream) throws IOException {
        byte[] hash = DigestUtils.md5(inputStream);
        inputStream.close();
        return hash;
    }

    @Override
    public DigestingInputStream digestingInputStream(InputStream inputStream) {
        return new DigestingInputStream<>(inputStream,
                new JavaDigestInputStream.Digester_MessageDigest(DigestUtils.getMd5Digest()));
    }
}
