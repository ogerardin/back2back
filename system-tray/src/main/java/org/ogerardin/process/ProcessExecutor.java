package org.ogerardin.process;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a convenient way to run a command and get its console output as a List of Strings.
 * The command and parameters to execute can be provided as either a String array through {@link #cmdarray} or a
 * command line as a single String through {@link #command}.
 * Appropriate for short-lived commands only.
 */
@Slf4j
@Builder
public class ProcessExecutor {

    private String command;
    private String[] cmdarray;
    private Path dir;
    private String[] envp;
    private String stdin;

    public ExecResults performExec() throws IOException, InterruptedException {
        if (dir == null) {
            dir =  Paths.get(".").toAbsolutePath();
        }
        Process process;
        if (cmdarray != null) {
            log.info("Executing {}", (Object[]) cmdarray);
            process = Runtime.getRuntime().exec(cmdarray, envp, dir.toFile());
        }
        else {
            log.info("Executing '{}'", command);
            process = Runtime.getRuntime().exec(command, envp, dir.toFile());
        }
        if (stdin != null) {
            new OutputStreamWriter(process.getOutputStream()).write(stdin);
        }
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());

        int exitValue = process.waitFor();

        log.debug("Process exited with value {}", exitValue);

        return new ExecResults(process, outputGobbler.getLines(), errorGobbler.getLines());
    }

    /**
     * A daemon Thread that reads the specified {@link InputStream} as long as EOF is not reached and stores
     * the read lines into a List. The Thread is started as soon as the constructor is called.
     */
    private class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private IOException exception = null;
        private List<String> lines = new ArrayList<>();

        StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
            this.setDaemon(true);
            this.start();
        }

        List<String> getLines() {
            return lines;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            catch (IOException ioe) {
                exception = ioe;
            }
        }
    }

    @Data
    public static class ExecResults {
        private final Process process;
        private final List<String> outputLines;
        private final List<String> errorLines;

        public int getExitValue() {
            return process.exitValue();
        }
    }


}
