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
    public byte[] md5Hash(InputStream is) throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer = new byte[1024];
        for(int read = is.read(buffer, 0, 1024); read > -1; read = is.read(buffer, 0, 1024)) {
            md.update(buffer, 0, read);
        }
        return md.digest();
    }
}
