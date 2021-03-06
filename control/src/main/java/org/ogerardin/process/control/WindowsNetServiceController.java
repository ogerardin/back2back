package org.ogerardin.process.control;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;

/**
 * Controller using Windows native "NET" command to control a Windows service.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class WindowsNetServiceController extends ExternalServiceController implements ProcessController {

    public WindowsNetServiceController(String serviceDisplayName) {
        super("net", serviceDisplayName);
    }

    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = String.format("cmd /c %s start | findstr \"%s\"", controllerCommand, escapeSpace(serviceName));
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
        mapExitCodeToException(performControllerServiceCommand("stop"));
    }

    @Override
    public void start() throws ControlException {
        mapExitCodeToException(performControllerServiceCommand("start"));
    }

    @Override
    public Long getPid() throws ControlException {
        return null;
    }

}
