package org.ogerardin.b2b.system_tray.processcontrol;

public interface ServiceController extends ProcessController {

    String getControllerInfo() throws ControlException;

    void installService(String[] commandLine) throws ControlException;

    void uninstallService() throws ControlException;

    boolean isAutostart() throws ControlException;

    void setAutostart(boolean autoStart) throws ControlException;
}
