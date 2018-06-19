package org.ogerardin.processcontrol;

public interface ServiceController extends ProcessController {

    void installService(String[] commandLine) throws ControlException;

    void uninstallService() throws ControlException;

    boolean isInstalled() throws ControlException;

    boolean isAutostart() throws ControlException;

    void setAutostart(boolean autoStart) throws ControlException;

}
