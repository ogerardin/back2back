package org.ogerardin.process.control;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Controls the execution lifecycle of a process instance. A single instance of the process is allowed.
 * The existence of the process is materialized by the existence of a file containing the PID number of the process.
 */
@Slf4j
@Data
@Builder
@AllArgsConstructor
public class NativeProcessController implements ProcessController {
    //FIXME we should really have a specialzed class per platform. This class tries to do all in one but it's awkward.

    private static final String OSNAME = System.getProperty("os.name").toLowerCase();

    /** Command to run and its arguments */
    private String[] command;

    /** Initial working directory of the process. */
    @Builder.Default private Path workDirectory = Paths.get(".");

    /** Name of the file containing the PID */
    @Builder.Default private String pidFileName = "pidfile.pid";

    /** Optional path of a file to use for redirecting process output */
    @Builder.Default private Path logFile = null;

    @Builder.Default ProcessListener processListener = null;


    public NativeProcessController(String[] command) {
        this.command = command;
    }

    @Override
    public boolean isRunning() throws ControlException {
        long pid;
        try {
            pid = readPid();
        } catch (FileNotFoundException e) {
            return false;
        }

        boolean running = isRunning(pid);
        if (!running) {
            //stale pid file
            deletePidFile();
        }
        return running;
    }

    private boolean isRunning(long pid) throws ControlException {
        try {
            String cmd = "ps -p " + pid;
            int exitValue = exec(cmd);
            return exitValue == 0;
        } catch (IOException | InterruptedException e) {
            log.debug("failed: {}", e.toString());
        }

        try {
            //findstr exit code 0 if found pid, 1 if it doesn't
            String cmd = "cmd /c \"tasklist /FI \"PID eq " + pid + "\" | findstr " + pid + "\"";
            int exitValue = exec(cmd);
            return exitValue == 0;
        } catch (IOException | InterruptedException e) {
            log.debug("failed: {}", e.toString());
        }

        throw new ControlException("Failed to check status of pid " + pid);
    }

    private int exec(String cmd) throws IOException, InterruptedException {
        log.debug("trying: {}", cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        int exitValue = p.exitValue();
        log.debug("returned: {}", exitValue);
        return exitValue;
    }

    public long readPid() throws FileNotFoundException, ControlException {
        Path pidFile = getPidFile();
        long pid;
        try (BufferedReader br = new BufferedReader(new FileReader(pidFile.toFile()))) {
            pid = Long.parseUnsignedLong(br.readLine());
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new ControlException("pid file unreadable", e);
        }
        return pid;
    }

    @Override
    public Long getPid() throws ControlException {
        try {
            return readPid();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public void stop() throws ControlException {
        log.debug("stopping process");
        long pid;
        try {
            pid = readPid();
            log.debug("read pid={}", pid);
        } catch (FileNotFoundException e) {
            log.debug("no pid file, assuming process already stopped");
            return;
        }

        if (!isRunning(pid)) {
            log.debug("invalid pid, deleting stale pid file");
            // stale pid file
            deletePidFile();
            return;
        }

        try {
            String cmd = "kill -9 " + pid;
            exec(cmd);
            deletePidFile();
            return;
        } catch (IOException | InterruptedException e) {
            log.debug("failed: {}", e.toString());
        }

        try {
            String cmd = "cmd /c \"taskkill /F /PID " + pid + "\"";
            exec(cmd);
            deletePidFile();
            return;
        } catch (IOException | InterruptedException e) {
            log.debug("failed: {}", e.toString());
        }

        throw new ControlException("Failed to stop process with pid " + pid);
    }

    private void deletePidFile() {
        try {
            Path pidFile = getPidFile();
            log.debug("removing pid file {}", pidFile);
            Files.delete(pidFile);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void start() throws ControlException {
        if (isRunning()) {
            throw new ControlException("already running");
        }

        log.info("starting {}", Arrays.toString(command));
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workDirectory.toFile());
        processBuilder.redirectErrorStream(true);

        if (logFile != null) {
            log.debug("output redirected to {}", logFile);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        }
        else {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }

        Process process;
        try {
            process = processBuilder.start();
            log.debug("process started: {}", process);
        } catch (IOException e) {
            throw new ControlException("failed to start process", e);
        }

        long pid;
        try {
            pid = getPid(process);
            log.debug("pid={}", pid);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ControlException("Failed to get pid of launched process", e);
        }

        try {
            writePid(pid);
        } catch (IOException e) {
            throw new ControlException("failed to write pid file", e);
        }

        Thread watcherThread = new Thread(() -> {
            try {
                process.waitFor();
                handleEvent(new ProcessEvent(process));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        watcherThread.start();
    }

    private void handleEvent(ProcessEvent processEvent) {
        if (processListener != null) {
            processListener.processTerminated(processEvent);
        }
    }

    private void writePid(long pid) throws IOException {
        Path pidFile = getPidFile();
        log.debug("writing pid to {}", pidFile);
        try (PrintStream ps = new PrintStream(new FileOutputStream(pidFile.toFile()))) {
            ps.print(pid);
        }
    }

    public Path getPidFile() {
        return workDirectory.resolve(pidFileName);
    }

    /**
     * @return the OS specific PID number identiying the specified {@link Process}
     */
    private long getPid(Process process) throws NoSuchFieldException, IllegalAccessException {
        // try "pid" field (works on Unixes)
        try {
            long pid = getLongField(process, "pid");
            return pid;
        } catch (NoSuchFieldException ignored) {
        }

        // try "handle" field (works on Windows)
        try {
            long handle = getLongField(process, "handle");
            // handle must be converted to a PID using jna wizardry
            Kernel32 kernel = Kernel32.INSTANCE;
            HANDLE winHandle = new HANDLE();
            winHandle.setPointer(Pointer.createConstant(handle));
            long pid = kernel.GetProcessId(winHandle);
            return pid;
        } catch (NoSuchFieldException ignored) {
        }

        throw new NoSuchFieldException("pid/handle");
    }

    /**
     * @return the value of the field (of type long) with the specified name, of the specified target object
     */
    private long getLongField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        long value = field.getLong(target);
        field.setAccessible(false);
        return value;
    }

    public interface ProcessListener {
        void processTerminated(ProcessEvent processEvent);
    }

    @Data
    public static class ProcessEvent {
        private final Process process;
    }

}
