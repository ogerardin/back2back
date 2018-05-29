package org.ogerardin.b2b.system_tray;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativeProcessController implements ProcessController {

    private static final String OSNAME = System.getProperty("os.name").toLowerCase();
    public static final String PIDFILE = "back2back.pid";

    private final String[] commandLine;
    private final Path homeDirectory;
    private final Path logFile;

    public NativeProcessController(String[] commandLine, Path homeDirectory, Path logFile) {
        this.commandLine = commandLine;
        this.homeDirectory = homeDirectory;
        this.logFile = logFile;
    }

    @Override
    public boolean isRunning() {
        Path pidFile = getPidFile();
        if (!Files.exists(pidFile)) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(getPidFile().toFile()))) {
            long pid = Long.parseUnsignedLong(br.readLine());
        } catch (IOException e) {
        }
        // TODO

        return false;
    }

    @Override
    public void stop() {

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

    public Path getPidFile() {
        return homeDirectory.resolve(PIDFILE);
    }

    private long getPid(Process process) throws NoSuchFieldException, IllegalAccessException {
        // try "pid" field (works on Unix)
        try {
            long pid = getField(process, "pid");
            return pid;
        } catch (NoSuchFieldException ignored) {
        }

        // try "handle" field (works on Windows)
        try {
            long handle = getField(process, "handle");
            Kernel32 kernel = Kernel32.INSTANCE;
            HANDLE winHandle = new HANDLE();
            winHandle.setPointer(Pointer.createConstant(handle));
            long pid = kernel.GetProcessId(winHandle);
            return pid;
        } catch (NoSuchFieldException ignored) {
        }

        throw new NoSuchFieldException("pid/handle");
    }


    private long getField(Process process, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = process.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        long value = field.getLong(process);
        field.setAccessible(false);
        return value;
    }

    @Override
    public void restart() {

    }
}
