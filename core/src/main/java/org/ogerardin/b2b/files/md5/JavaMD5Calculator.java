package org.ogerardin.b2b.files.md5;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 hash calculator using native Java
 */
@Component
public class JavaMD5Calculator implements MD5Calculator, StreamingMd5Calculator {

    private static final int BUFFER_SIZE = 1024;

    @Override
    public byte[] md5Hash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] md5Hash(InputStream inputStream) throws IOException {
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
}
