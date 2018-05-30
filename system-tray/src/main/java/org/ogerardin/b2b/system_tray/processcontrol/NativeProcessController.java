package org.ogerardin.b2b.system_tray.processcontrol;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import lombok.Builder;
import lombok.Data;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Builder
public class NativeProcessController implements ProcessController {

    private static final String OSNAME = System.getProperty("os.name").toLowerCase();

    @Builder.Default private String pidfile = "pidfile.pid";
    @Builder.Default private Path homeDirectory = Paths.get(".");
    private String[] commandLine;
    private Path logFile;

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
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException ignored) {
        }

        try {
            //findstr exit code 0 if found pid, 1 if it doesn't
            String cmd = "cmd /c \"tasklist /FI \"PID eq " + pid + "\" | findstr " + pid + "\"";
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException ignored) {
        }

        throw new ControlException("Failed to check status of pid " + pid);
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
    public void stop() throws ControlException {

        long pid;
        try {
            pid = readPid();
        } catch (FileNotFoundException e) {
            return;
        }

        if (!isRunning(pid)) {
            // stale pid file
            deletePidFile();
            return;
        }

        try {
            String cmd = "kill -9 " + pid;
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            deletePidFile();
            return;
        } catch (IOException | InterruptedException ignored) {
        }

        try {
            String cmd = "cmd /c \"taskkill /F /PID " + pid + "\"";
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            deletePidFile();
            return;
        } catch (IOException | InterruptedException ignored) {
        }

        throw new ControlException("Failed to stop process with pid " + pid);
    }

    private void deletePidFile() {
        try {
            Files.delete(getPidFile());
        } catch (IOException ignored) {
        }
    }

    @Override
    public void start() throws ControlException {
        if (isRunning()) {
            throw new ControlException("already running");
        }

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.directory(homeDirectory.toFile());

        if (logFile != null) {
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        }

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new ControlException("failed to start process", e);
        }

        long pid;
        try {
            pid = getPid(process);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ControlException("Failed to get pid of launched process", e);
        }

        try {
            writePid(pid);
        } catch (IOException e) {
            throw new ControlException("failed to write pid file", e);
        }
    }

    private void writePid(long pid) throws IOException {
        Path pidFile = getPidFile();
        try (PrintStream ps = new PrintStream(new FileOutputStream(pidFile.toFile()))) {
            ps.print(pid);
        }
    }

    public Path getPidFile() {
        return homeDirectory.resolve(pidfile);
    }

    /**
     * @return the OS specific number identiying the specified {@link Process}
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

    @Override
    public void restart() throws ControlException {
        stop();
        start();
    }
}
