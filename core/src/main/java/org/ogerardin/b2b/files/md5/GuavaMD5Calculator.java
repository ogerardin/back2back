package org.ogerardin.b2b.files.md5;

import com.google.common.hash.Hashing;
import org.springframework.stereotype.Component;

@Component
public class GuavaMD5Calculator implements MD5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        return Hashing.md5().hashBytes(bytes).asBytes();
    }
}
