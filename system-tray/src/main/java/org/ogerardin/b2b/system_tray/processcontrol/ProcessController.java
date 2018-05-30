package org.ogerardin.b2b.system_tray.processcontrol;

public interface ProcessController {

    boolean isRunning() throws ControlException;

    void stop() throws ControlException;

    void start() throws ControlException;

    void restart() throws ControlException;
}
