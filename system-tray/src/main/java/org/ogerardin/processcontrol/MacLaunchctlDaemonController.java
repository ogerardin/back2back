package org.ogerardin.processcontrol;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.ogerardin.process.ProcessExecutor.ExecResults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Controller using Mac native "launchctl" command to control a macOS Daemon
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class MacLaunchctlDaemonController extends ExternalServiceController implements ServiceController {

    public MacLaunchctlDaemonController(String jobName) {
        super("launchctl", jobName);
    }

    @Override
    public boolean isRunning() throws ControlException {
        try {
            String cmd = buildCommandString(controllerCommand, "list", serviceName);
            Process p = Runtime.getRuntime().exec(cmd);
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                line = reader.readLine();
            }
            p.waitFor();
            if (line == null) {
                // no result -> job not loaded
                return false;
            }

            // output format is "<pid> <last-exit-code> <job-name>"
            String[] items = line.split(" +");

            long pid = 0;
            try {
                pid = Long.parseUnsignedLong(items[0]);
            } catch (NumberFormatException ignored) {
            }
            // pid is numeric -> job is running
            return pid != 0;
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Failed to get service status", e);
        }
    }

    @Override
    public void stop() throws ControlException {
        // assumes the job has been loaded
        ExecResults r = performControllerServiceCommand("stop");
        mapExitCodeToException(r);

    }

    @Override
    public void start() throws ControlException {
        // assumes the job has been loaded
        ExecResults r = performControllerServiceCommand("start");
        mapExitCodeToException(r);
    }

    @Override
    public Long getPid() throws ControlException {
        return null;
    }

    @Override
    protected String buildCommandString(String controller, String command, String serviceName, String... args) {
        return String.format("sudo %s", super.buildCommandString(this.controllerCommand, command, this.serviceName));
    }

    @Override
    public void installService(String[] commandLine) throws ControlException {
        //TODO
    }

    @Override
    public void uninstallService() throws ControlException {
        //TODO
    }

    @Override
    public boolean isAutostart() throws ControlException {
        //TODO
        return false;
    }

    @Override
    public void setAutostart(boolean autoStart) throws ControlException {
        //TODO
    }

    @Override
    public boolean isInstalled() throws ControlException {
        //TODO
        return false;
    }
}
