package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Builder;
import lombok.Data;

import java.io.IOException;

/**
 * Controller using "nssm" (https://nssm.cc/) to control a Windows service.
 */
@Builder
@Data
public class WindowsNssmServiceController extends ExternalServiceController implements ProcessController {

    public WindowsNssmServiceController(String serviceName) {
        super("nssm", serviceName);
    }

    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = String.format("cmd /c \"%s status %s\" | findstr SERVICE_RUNNING\"", controller, serviceName);
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Failed to get service status", e);
        }
    }

    @Override
    public void stop() throws ControlException {
        performControllerCommand("stop");
    }

    @Override
    public void start() throws ControlException {
        performControllerCommand("start");
    }

    @Override
    public void restart() throws ControlException {
        performControllerCommand("restart");
    }

}


