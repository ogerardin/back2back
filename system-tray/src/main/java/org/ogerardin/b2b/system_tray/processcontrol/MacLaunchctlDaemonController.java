package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Builder;
import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Controller using Mac native "launchctl" command to control a macOS Daemon
 */
@Builder
@Data
public class MacLaunchctlDaemonController extends ExternalServiceController implements ProcessController {

    public MacLaunchctlDaemonController(String controller, String serviceName) {
        super("launchctl", serviceName);
    }

    @Override
    public boolean isRunning() throws ControlException {
        try {
            String cmd = buildCommandString(controller, "list", serviceName);
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
        performControllerCommand("stop");
    }

    @Override
    public void start() throws ControlException {
        // assumes the job has been loaded
        performControllerCommand("start");
    }

    @Override
    protected String buildCommandString(String controller, String command, String serviceName) {
        return String.format("sudo %s", super.buildCommandString(this.controller, command, this.serviceName));
    }
}
