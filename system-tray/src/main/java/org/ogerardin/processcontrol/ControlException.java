package org.ogerardin.processcontrol;

public class ControlException extends Exception {
    public ControlException(String s) {
        super(s);
    }

    public ControlException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ControlException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        String msg = super.toString();
        if (getCause() != null) {
            msg += " - " + getCause().toString();
        }
        return msg;
    }
}
