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

package org.ogerardin.b2b.hash.md5.fast;

import com.twmacinta.util.MD5;
import org.ogerardin.b2b.hash.Digester;
import org.ogerardin.b2b.hash.DigestingInputStream;
import org.ogerardin.b2b.util.Container;

import java.io.InputStream;

@SuppressWarnings("UnstableApiUsage")
class FastMd5DigestInputStream extends DigestingInputStream {

    FastMd5DigestInputStream(InputStream stream) {
        super(stream, new Digester_MD5(new MD5()));
    }


    public static class Digester_MD5 extends Container<MD5> implements Digester {

        Digester_MD5(MD5 md5) {
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
