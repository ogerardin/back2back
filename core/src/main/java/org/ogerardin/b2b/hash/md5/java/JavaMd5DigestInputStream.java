package org.ogerardin.b2b.hash.md5.java;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("UnstableApiUsage")
public class JavaMd5DigestInputStream extends JavaDigestInputStream {

    @SuppressWarnings("deprecation")
    public JavaMd5DigestInputStream(InputStream stream) throws NoSuchAlgorithmException {
        super(stream, MessageDigest.getInstance("MD5"));
    }

}
