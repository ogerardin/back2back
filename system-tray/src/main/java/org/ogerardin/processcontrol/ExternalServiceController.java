package org.ogerardin.processcontrol;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass for a {@link ProcessController} that uses an external controller.
 */
@Data
public abstract class ExternalServiceController implements ProcessController {

    protected final String controller;
    protected final String serviceName;
    protected Path workDir;

    protected ExternalServiceController(String controller, String serviceName) {
        this.controller = controller;
        this.serviceName = serviceName;
    }

    protected ExecResults performControllerServiceCommand(String command, String... args) throws ControlException {
        if (workDir == null) {
             workDir =  Paths.get(".").toAbsolutePath();
        }
        String cmd = buildCommandString(controller, command, serviceName, args);
        try {
            return performExec(cmd);
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Controller command failed: " + command, e);
        }
    }

    protected ExecResults performControllerCommand(String command, String... args) throws ControlException {
        if (workDir == null) {
             workDir =  Paths.get(".").toAbsolutePath();
        }
        String additionalArgs = String.join(" ", args);
        String cmd = String.format("%s %s %s", controller, command, additionalArgs);
        try {
            return performExec(cmd);
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Controller command failed: " + command, e);
        }
    }

    private ExecResults performExec(String cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd, null, workDir.toFile());
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());
        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
        p.waitFor();
        return new ExecResults(p.exitValue(), outputGobbler.getLines(), errorGobbler.getLines());
    }

    protected void mapExitCodeToException(ExecResults execResults) throws ControlException {
        if (execResults.getExitValue() != 0) {
            throw new ControlException(String.format("Command returned non-0 exit code: %d", execResults.getExitValue()));
        }
    }


    protected String buildCommandString(String controller, String command, String serviceName, String... args) {
        String additionalArgs = String.join(" ", args);
        return String.format("%s %s %s %s", controller, command, serviceName, additionalArgs);
    }

    private class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private IOException exception = null;
        private List<String> lines = new ArrayList<>();

        private StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public List<String> getLines() {
            return lines;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null)
                    lines.add(line);
            }
            catch (IOException ioe) {
                exception = ioe;
            }
        }
    }

    @Data
    public static class ExecResults {
        private final int exitValue;
        private final List<String> outputLines;
        private final List<String> errorLines;
    }
}
