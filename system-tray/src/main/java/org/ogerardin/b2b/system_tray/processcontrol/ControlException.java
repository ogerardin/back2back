package org.ogerardin.b2b.system_tray.processcontrol;

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

}
