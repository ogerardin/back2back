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

package org.ogerardin.b2b.hash;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An {@link InputStream} decorator that updates a provided {@link Digester} object as bytes are read.
 * Based on {@link java.security.DigestInputStream}, but using a generic {@link Digester} instead of
 * {@link java.security.MessageDigest}
 *
 * @param <D> the {@link Digester} class
 */
public class DigestingInputStream<D extends Digester> extends FilterInputStream {

    /* Are we on or off? */
    private boolean on = true;

    private final D digester;

    public DigestingInputStream(InputStream stream, D digester) {
        super(stream);
        this.digester = digester;
    }

    public int read() throws IOException {
        int ch = in.read();
        if (on && ch != -1) {
            digester.putByte((byte)ch);
        }
        return ch;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (on && result != -1) {
            digester.putBytes(b, off, result);
        }
        return result;
    }

    public void on(boolean on) {
        this.on = on;
    }

    public byte[] hash() {
        return digester.hash();
    }
}
