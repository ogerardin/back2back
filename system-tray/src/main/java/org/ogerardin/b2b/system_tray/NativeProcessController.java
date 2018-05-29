package org.ogerardin.b2b.system_tray;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import lombok.Builder;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

@Builder
public class NativeProcessController implements ProcessController {

    private static final String OSNAME = System.getProperty("os.name").toLowerCase();

    @Builder.Default private String pidfile = "pidfile.pid";
    @Builder.Default private Path homeDirectory = Paths.get(".");
    private String[] commandLine;
    private Path logFile;

    @Override
    public boolean isRunning() {
        long pid;
        try {
            pid = readPid();
        } catch (FileNotFoundException e) {
            return false;
        }

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

        throw new RuntimeException("Failed to check pid");
    }

    private long readPid() throws FileNotFoundException {
        Path pidFile = getPidFile();
        long pid;
        try (BufferedReader br = new BufferedReader(new FileReader(pidFile.toFile()))) {
            pid = Long.parseUnsignedLong(br.readLine());
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("pid file " + pidFile + "unreadable");
        }
        return pid;
    }

    @Override
    public void stop() {
        long pid;
        try {
            pid = readPid();
        } catch (FileNotFoundException e) {
            // not running
            return;
        }

        try {
            String cmd = "kill -9 " + pid;
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return;
        } catch (IOException | InterruptedException ignored) {
        }

        try {
            String cmd = "cmd /c \"taskkill /FI \"PID eq " + pid + "\" | findstr " + pid + "\"";
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return;
        } catch (IOException | InterruptedException ignored) {
        }

        throw new RuntimeException("Failed to stop process with pid " + pid);
    }

    @Override
    public void start() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.directory(homeDirectory.toFile());

        if (logFile != null) {
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        }

        Process process = processBuilder.start();

        long pid;
        try {
            pid = getPid(process);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get pid");
        }

        Path pidFile = getPidFile();
        try (PrintStream ps = new PrintStream(new FileOutputStream(pidFile.toFile()))) {
            ps.print(pid);
        }
    }

    private Path getPidFile() {
        return homeDirectory.resolve(pidfile);
    }

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
            Kernel32 kernel = Kernel32.INSTANCE;
            HANDLE winHandle = new HANDLE();
            winHandle.setPointer(Pointer.createConstant(handle));
            long pid = kernel.GetProcessId(winHandle);
            return pid;
        } catch (NoSuchFieldException ignored) {
        }

        throw new NoSuchFieldException("pid/handle");
    }


    private long getLongField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        long value = field.getLong(target);
        field.setAccessible(false);
        return value;
    }

    @Override
    public void restart() throws IOException {
        stop();
        start();
    }
}
