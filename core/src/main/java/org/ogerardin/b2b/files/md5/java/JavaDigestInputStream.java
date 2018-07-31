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

package org.ogerardin.b2b.files.md5.java;

import org.ogerardin.b2b.files.md5.HashProviderInputStream;
import org.ogerardin.b2b.files.md5.UpdatableHashProvider;
import org.ogerardin.b2b.util.Container;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * An implemetation of {@link HashProviderInputStream} that updates a Java native {@link MessageDigest}.
 */
@SuppressWarnings("UnstableApiUsage")
public class JavaDigestInputStream extends HashProviderInputStream {

    public JavaDigestInputStream(InputStream stream, MessageDigest messageDigest) {
        super(stream, new UpdatableDigest(messageDigest));
    }


    private static class UpdatableDigest extends Container<MessageDigest> implements UpdatableHashProvider<MessageDigest> {

        UpdatableDigest(MessageDigest messageDigest) {
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
