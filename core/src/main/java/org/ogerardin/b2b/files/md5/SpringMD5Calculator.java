package org.ogerardin.b2b.files.md5;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * MD5 hash calculator using Spring's {@link DigestUtils}
 */
@Component
public class SpringMD5Calculator implements MD5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        return DigestUtils.md5Digest(bytes);
    }
}
