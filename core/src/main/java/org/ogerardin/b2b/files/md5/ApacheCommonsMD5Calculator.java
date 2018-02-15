package org.ogerardin.b2b.files.md5;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;


/**
 * MD5 hash calculator using Apache commons codec.
 * Most performant on average.
 */
@Component
public class ApacheCommonsMD5Calculator implements MD5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        return DigestUtils.md5(bytes);
    }
}
