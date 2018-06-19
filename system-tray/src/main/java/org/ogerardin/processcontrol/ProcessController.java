package org.ogerardin.processcontrol;

public interface ProcessController {

    boolean isRunning() throws ControlException;

    void stop() throws ControlException;

    void start() throws ControlException;

    default void restart() throws ControlException {
        stop();
        start();
    }

    Long getPid() throws ControlException;
}
