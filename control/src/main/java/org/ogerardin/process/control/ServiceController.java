package org.ogerardin.process.control;

public interface ServiceController extends ProcessController {

    void assertReady() throws ControlException;

    void installService(String[] commandLine) throws ControlException;

    void uninstallService() throws ControlException;

    boolean isInstalled() throws ControlException;

    boolean isAutostart() throws ControlException;

    void setAutostart(boolean autoStart) throws ControlException;
}
