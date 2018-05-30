package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class WindowsNetServiceController implements ProcessController {

    private static final String NSSM = "nssm";

    private final String serviceName;
    private final String nssmDir;

    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = "cmd /c \"nssm status " + serviceName + "\" | findstr SERVICE_RUNNING\"";
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Failed to get service status", e);
        }
    }

    @Override
    public void stop() throws ControlException {
        performNssmCommand("stop");
    }

    @Override
    public void start() throws ControlException {
        performNssmCommand("start");
    }

    @Override
    public void restart() throws ControlException {
        performNssmCommand("restart");
    }

    private void performNssmCommand(String command) throws ControlException {
        try {
            String cmd = String.format("%s %s %s", NSSM, command, serviceName);
            Path nssmDirPath = Paths.get(nssmDir);
            Process p = Runtime.getRuntime().exec(cmd, null, nssmDirPath.toFile());
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ControlException("nssm command failed: " + command, e);
        }
    }
}
