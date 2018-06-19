package org.ogerardin.processcontrol;

public class ServiceNotFoundException extends ControlException {
    public ServiceNotFoundException(String s) {
        super(s);
    }

    public ServiceNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ServiceNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
