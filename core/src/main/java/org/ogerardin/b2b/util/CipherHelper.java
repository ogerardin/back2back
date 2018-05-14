package org.ogerardin.b2b.util;

import org.ogerardin.b2b.storage.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public enum CipherHelper {
    ;


    public static Cipher getAesCipher(Key key, int encryptMode) throws EncryptionException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(encryptMode, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new EncryptionException("Exception while initializing Cipher", e);
        }
        return cipher;
    }
}
