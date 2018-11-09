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

package org.ogerardin.b2b.hash.md5.guava;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import org.ogerardin.b2b.hash.Digester;
import org.ogerardin.b2b.hash.DigestingInputStream;
import org.ogerardin.b2b.util.Container;

import java.io.InputStream;

@SuppressWarnings("UnstableApiUsage")
class GuavaDigestInputStream extends DigestingInputStream {

    GuavaDigestInputStream(InputStream stream, HashFunction hashFunction) {
        super(stream, new Digester_Hasher(hashFunction.newHasher()));
    }

    private static class Digester_Hasher extends Container<Hasher> implements Digester {

        Digester_Hasher(Hasher hasher) {
            super(hasher);
        }

        @Override
        public void putByte(byte b) {
            get().putByte(b);
        }

        @Override
        public void putBytes(byte[] b, int off, int result) {
            get().putBytes(b, off, result);
        }

        @Override
        public byte[] hash() {
            return get().hash().asBytes();
        }
    }
}
