package org.ogerardin.b2b.files.md5.java;

import org.ogerardin.b2b.files.md5.HashProviderInputStream;
import org.ogerardin.b2b.files.md5.InputStreamMD5Calculator;
import org.ogerardin.b2b.files.md5.ByteArrayMD5Calculator;
import org.ogerardin.b2b.files.md5.MD5UpdatingInputStreamProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 hash calculator using native Java
 */
@Component
public class JavaMD5Calculator implements ByteArrayMD5Calculator, InputStreamMD5Calculator, MD5UpdatingInputStreamProvider {

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

    @Override
    public HashProviderInputStream md5UpdatingInputStream(InputStream inputStream) {
        try {
            return new JavaMd5DigestInputStream(inputStream);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
