package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Abstract superclass for a {@link ProcessController} that uses an external controller.
 */
@Data
public abstract class ExternalServiceController implements ProcessController {

    protected final String controller;
    protected final String serviceName;
    @Builder.Default protected Path workDir =  Paths.get(".");

    protected void performControllerCommand(String command) throws ControlException {
        try {
            String cmd = buildCommandString(controller, command, serviceName);
            Process p = Runtime.getRuntime().exec(cmd, null, workDir.toFile());
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Controller command failed: " + command, e);
        }
    }

    protected String buildCommandString(String controller, String command, String serviceName) {
        return String.format("%s %s %s", controller, command, serviceName);
    }
}
