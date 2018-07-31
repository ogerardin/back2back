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

package org.ogerardin.b2b.files.md5;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UpdaterInputStream<U extends Updatable> extends FilterInputStream {

    /* Are we on or off? */
    private boolean on = true;

    private final U updatable;

    public UpdaterInputStream(InputStream stream, U updatable) {
        super(stream);
        this.updatable = updatable;
    }

    public int read() throws IOException {
        int ch = in.read();
        if (on && ch != -1) {
            updatable.putByte((byte)ch);
        }
        return ch;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (on && result != -1) {
            updatable.putBytes(b, off, result);
        }
        return result;
    }

    public void on(boolean on) {
        this.on = on;
    }

    public U getUpdatable() {
        return updatable;
    }
}
