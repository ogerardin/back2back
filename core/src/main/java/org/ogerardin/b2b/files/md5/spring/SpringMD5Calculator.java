package org.ogerardin.b2b.files.md5.spring;

import org.ogerardin.b2b.files.md5.InputStreamMd5Calculator;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * MD5 hash calculator using Spring's {@link DigestUtils}
 */
@Component
public class SpringMD5Calculator implements MD5Calculator, InputStreamMd5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        return DigestUtils.md5Digest(bytes);
    }

    @Override
    public byte[] md5Hash(InputStream inputStream) throws IOException {
        byte[] hash = DigestUtils.md5Digest(inputStream);
        inputStream.close();
        return hash;
    }
}
