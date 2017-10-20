package org.ogerardin.b2b.files;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class JavaMD5Calculator implements MD5Calculator {

    @Override
    public byte[] computeMD5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
