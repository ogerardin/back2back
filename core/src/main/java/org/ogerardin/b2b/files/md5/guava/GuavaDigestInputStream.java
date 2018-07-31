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
import org.ogerardin.b2b.files.md5.HashProviderInputStream;
import org.ogerardin.b2b.files.md5.UpdatableHashProvider;
import org.ogerardin.b2b.util.Container;

import java.io.InputStream;

@SuppressWarnings("UnstableApiUsage")
public class GuavaDigestInputStream extends HashProviderInputStream {

    public GuavaDigestInputStream(InputStream stream, HashFunction hashFunction) {
        super(stream, new UpdatableHasher(hashFunction.newHasher()));
    }


    private static class UpdatableHasher extends Container<Hasher> implements UpdatableHashProvider<Hasher> {

        UpdatableHasher(Hasher hasher) {
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
