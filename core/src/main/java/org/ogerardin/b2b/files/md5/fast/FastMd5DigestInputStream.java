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

package org.ogerardin.b2b.files.md5.fast;

import com.twmacinta.util.MD5;
import org.ogerardin.b2b.util.Container;
import org.ogerardin.b2b.files.md5.HashProviderInputStream;
import org.ogerardin.b2b.files.md5.UpdatableHashProvider;

import java.io.InputStream;

@SuppressWarnings("UnstableApiUsage")
public class FastMd5DigestInputStream extends HashProviderInputStream {

    public FastMd5DigestInputStream(InputStream stream) {
        super(stream, new UpdatableMD5(new MD5()));
    }


    public static class UpdatableMD5 extends Container<MD5> implements UpdatableHashProvider<MD5> {

        UpdatableMD5(MD5 md5) {
            super(md5);
        }

        @Override
        public void putByte(byte b) {
            get().Update(b);
        }

        @Override
        public void putBytes(byte[] b, int off, int result) {
            get().Update(b, off, result);
        }

        @Override
        public byte[] hash() {
            return get().Final();
        }
    }
}
