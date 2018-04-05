package org.ogerardin.b2b.files.md5;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;


/**
 * MD5 hash calculator using Apache commons codec.
 * Most performant on average.
 */
@Component
public class ApacheCommonsMD5Calculator implements MD5Calculator, StreamingMd5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        return DigestUtils.md5(bytes);
    }

    @Override
    public byte[] md5Hash(InputStream inputStream) throws IOException {
        byte[] hash = DigestUtils.md5(inputStream);
        inputStream.close();
        return hash;
    }
}
