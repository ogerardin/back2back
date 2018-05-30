package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Data;

import java.io.IOException;

@Data
public class WindowsNssmServiceController implements ProcessController {

    private static final String NET = "net";

    private final String serviceDisplayName;

    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = "cmd /c net start | findstr \"" + escapeSpace(serviceDisplayName) +"\"";
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Failed to get service status", e);
        }
    }

    private String escapeSpace(String str) {
        return str.replaceAll(" ", "^ ");
    }

    @Override
    public void stop() throws ControlException {
        performNetCommand("stop");
    }

    @Override
    public void start() throws ControlException {
        performNetCommand("start");
    }

    @Override
    public void restart() throws ControlException {
        stop();
        start();
    }

    private void performNetCommand(String command) throws ControlException {
        try {
            String cmd = String.format("%s %s \"%s\"", NET, command, escapeSpace(serviceDisplayName));
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ControlException("nssm command failed: " + command, e);
        }
    }
}
