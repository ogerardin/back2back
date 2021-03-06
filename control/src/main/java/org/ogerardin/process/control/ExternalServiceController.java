package org.ogerardin.process.control;

import lombok.Data;
import org.ogerardin.process.execute.ProcessExecutor;
import org.ogerardin.process.execute.ExecResults;

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

    protected ExecResults performControllerServiceCommand(String command, String... args) throws ControlException {
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

    protected ExecResults performControllerCommand(String command, String... args) throws ControlException {
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

    protected void mapExitCodeToException(ExecResults execResults) throws ControlException {
        if (execResults.getExitValue() != 0) {
            throw new ControlException(String.format("Command returned non-zero exit value: %d", execResults.getExitValue()));
        }
    }


    protected String buildCommandString(String controller, String command, String serviceName, String... args) {
        String additionalArgs = String.join(" ", args);
        return String.format("%s %s %s %s", controller, command, serviceName, additionalArgs);
    }

}
