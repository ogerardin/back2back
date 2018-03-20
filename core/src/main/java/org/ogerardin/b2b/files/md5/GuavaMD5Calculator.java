package org.ogerardin.b2b.files.md5;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;


/**
 * MD5 hash calculator using native Guava
 */
@Component
public class GuavaMD5Calculator implements MD5Calculator, StreamingMd5Calculator {

    @Override
    public byte[] md5Hash(byte[] bytes) {
        return Hashing.md5().hashBytes(bytes).asBytes();
    }

    @Override
    public byte[] md5Hash(InputStream is) throws IOException {
        Hasher hasher = Hashing.md5().newHasher();
        byte[] buffer = new byte[1024];
        for(int read = is.read(buffer, 0, 1024); read > -1; read = is.read(buffer, 0, 1024)) {
            hasher.putBytes(buffer, 0, read);
        }
        return hasher.hash().asBytes();
    }
}
