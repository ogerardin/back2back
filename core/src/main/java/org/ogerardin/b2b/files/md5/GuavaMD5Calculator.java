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

    private static final int BUFFER_SIZE = 1024;

    @Override
    public byte[] md5Hash(byte[] bytes) {
        @SuppressWarnings("deprecation")
        byte[] hash = Hashing.md5().hashBytes(bytes).asBytes();
        return hash;
    }

    @Override
    public byte[] md5Hash(InputStream inputStream) throws IOException {
        @SuppressWarnings("deprecation")
        Hasher hasher = Hashing.md5().newHasher();
        byte[] buffer = new byte[BUFFER_SIZE];
        for(int read = inputStream.read(buffer, 0, buffer.length); read > -1; read = inputStream.read(buffer, 0, buffer.length)) {
            hasher.putBytes(buffer, 0, read);
        }
        byte[] hash = hasher.hash().asBytes();
        inputStream.close();
        return hash;
    }
}
