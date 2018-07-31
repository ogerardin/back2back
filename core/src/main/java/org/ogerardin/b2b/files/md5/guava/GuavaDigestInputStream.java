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

package org.ogerardin.b2b.files.md5.guava;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import org.ogerardin.b2b.files.md5.Updatable;
import org.ogerardin.b2b.files.md5.UpdaterInputStream;

import java.io.InputStream;

public class GuavaDigestInputStream extends UpdaterInputStream<GuavaDigestInputStream.UpdatableHasher> {

    public GuavaDigestInputStream(InputStream stream, HashFunction hashFunction) {
        super(stream, new UpdatableHasher(hashFunction.newHasher()));
    }

    public static class UpdatableHasher implements Updatable<Hasher> {

        private final Hasher hasher;

        public UpdatableHasher(Hasher hasher) {
            this.hasher = hasher;
        }

        @Override
        public void putByte(byte b) {
            hasher.putByte(b);
        }

        @Override
        public void putBytes(byte[] b, int off, int result) {
            hasher.putBytes(b, off, result);
        }

        public Hasher get() {
            return hasher;
        }

    }
}
