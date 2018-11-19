package org.ogerardin.process.execute;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides a convenient way to run a command and get its console output as a List of Strings.
 * The command and parameters to execute can be provided as either a String array through {@link #cmdarray} or a
 * command line as a single String through {@link #command}.
 * Appropriate for short-lived commands only.
 */
@Slf4j
@ToString
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


}
