package org.ogerardin.b2b;

public class B2BException extends Exception{

    public B2BException(String s) {
        super(s);
    }

    public B2BException(String s, Throwable e) {
        super(s, e);
    }
}
