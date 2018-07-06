package org.ogerardin.processcontrol;

import lombok.Data;
import org.ogerardin.process.ProcessExecutor;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract superclass for a {@link ProcessController} that uses an external controller.
 */
@Data
public abstract class ExternalServiceController implements ProcessController {

    protected final String controllerCommand;
    protected final String serviceName;
    protected Path workDir;

    protected ExternalServiceController(String controllerCommand, String serviceName) {
        this.controllerCommand = controllerCommand;
        this.serviceName = serviceName;
    }

    protected ProcessExecutor.ExecResults performControllerServiceCommand(String command, String... args) throws ControlException {
        String cmd = buildCommandString(controllerCommand, command, serviceName, args);
        try {
            ProcessExecutor executor = ProcessExecutor.builder()
                    .command(cmd)
                    .dir(workDir)
                    .build();
            return executor.performExec();
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Controller command failed: " + command, e);
        }
    }

    protected ProcessExecutor.ExecResults performControllerCommand(String command, String... args) throws ControlException {
        String additionalArgs = String.join(" ", args);
        String cmd = String.format("%s %s %s", controllerCommand, command, additionalArgs);
        ProcessExecutor executor = ProcessExecutor.builder()
                .command(cmd)
                .dir(workDir)
                .build();
        try {
            return executor.performExec();
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Controller command failed: " + command, e);
        }
    }

    protected void mapExitCodeToException(ProcessExecutor.ExecResults execResults) throws ControlException {
        if (execResults.getExitValue() != 0) {
            throw new ControlException(String.format("Command returned non-0 exit code: %d", execResults.getExitValue()));
        }
    }


    protected String buildCommandString(String controller, String command, String serviceName, String... args) {
        String additionalArgs = String.join(" ", args);
        return String.format("%s %s %s %s", controller, command, serviceName, additionalArgs);
    }

}
