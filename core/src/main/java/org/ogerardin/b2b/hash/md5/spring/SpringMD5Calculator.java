package org.ogerardin.b2b.hash.md5.spring;

import org.ogerardin.b2b.hash.InputStreamHashCalculator;
import org.ogerardin.b2b.hash.ByteArrayHashCalculator;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * MD5 hash calculator using Spring's {@link DigestUtils}
 */
@Component
public class SpringMD5Calculator implements ByteArrayHashCalculator, InputStreamHashCalculator {

    @Override
    public byte[] hash(byte[] bytes) {
        return DigestUtils.md5Digest(bytes);
    }

    @Override
    public byte[] hash(InputStream inputStream) throws IOException {
        byte[] hash = DigestUtils.md5Digest(inputStream);
        inputStream.close();
        return hash;
    }
}
