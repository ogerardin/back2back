package org.ogerardin.b2b.hash.md5.java;

import org.ogerardin.b2b.hash.DigestingInputStream;
import org.ogerardin.b2b.hash.ByteArrayHashCalculator;
import org.ogerardin.b2b.hash.InputStreamHashCalculator;
import org.ogerardin.b2b.hash.DigestingInputStreamProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 hash calculator using native Java
 */
@Component
public class JavaMD5Calculator implements ByteArrayHashCalculator, InputStreamHashCalculator, DigestingInputStreamProvider {

    private static final int BUFFER_SIZE = 1024;

    @Override
    public byte[] hash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] hash(InputStream inputStream) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        for(int read = inputStream.read(buffer, 0, buffer.length); read > -1; read = inputStream.read(buffer, 0, buffer.length)) {
            md.update(buffer, 0, read);
        }
        byte[] hash = md.digest();
        inputStream.close();
        return hash;
    }

    @Override
    public DigestingInputStream digestingInputStream(InputStream inputStream) {
        try {
            return new JavaMd5DigestInputStream(inputStream);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
