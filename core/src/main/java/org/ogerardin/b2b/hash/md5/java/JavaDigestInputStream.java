/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package org.ogerardin.b2b.hash.md5.java;

import org.ogerardin.b2b.hash.Digester;
import org.ogerardin.b2b.hash.DigestingInputStream;
import org.ogerardin.b2b.util.Container;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * An implemetation of {@link DigestingInputStream} that uses a Java native {@link MessageDigest}.
 */
@SuppressWarnings("UnstableApiUsage")
public class JavaDigestInputStream extends DigestingInputStream<JavaDigestInputStream.Digester_MessageDigest> {

    public JavaDigestInputStream(InputStream stream, MessageDigest messageDigest) {
        super(stream, new Digester_MessageDigest(messageDigest));
    }


    static class Digester_MessageDigest extends Container<MessageDigest> implements Digester {

        Digester_MessageDigest(MessageDigest messageDigest) {
            super(messageDigest);
        }

        @Override
        public void putByte(byte b) {
            get().update(b);
        }

        @Override
        public void putBytes(byte[] b, int off, int result) {
            get().update(b, off, result);
        }

        @Override
        public byte[] hash() {
            return get().digest();
        }
    }
}
