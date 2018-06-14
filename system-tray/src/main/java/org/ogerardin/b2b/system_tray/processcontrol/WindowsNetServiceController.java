package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Builder;
import lombok.Data;

import java.io.IOException;

/**
 * Controller using Windows native "NET" command to control a Windows service.
 */
@Builder
@Data
public class WindowsNetServiceController extends ExternalServiceController implements ProcessController {

    public WindowsNetServiceController(String controller, String serviceDisplayName) {
        super("net", serviceDisplayName);
    }

    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = String.format("cmd /c %s start | findstr \"%s\"", controller, escapeSpace(serviceName));
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Failed to get service status", e);
        }
    }

    private static String escapeSpace(String str) {
        return str.replaceAll(" ", "^ ");
    }

    @Override
    public void stop() throws ControlException {
        performControllerCommand("stop");
    }

    @Override
    public void start() throws ControlException {
        performControllerCommand("start");
    }

}
