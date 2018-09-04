package org.ogerardin.process.control;

import com.sun.jna.Platform;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.ogerardin.process.execute.ExecResults;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller using "nssm" (https://nssm.cc/) to control a Windows service.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Slf4j
public class WindowsNssmServiceController extends ExternalServiceController implements ServiceController {

    private static Path nssmExe;

    public WindowsNssmServiceController(String serviceName) {
        this(getNssmExePath(), serviceName);
    }

    public WindowsNssmServiceController(Path nssmExePath, String serviceName) {
        super(nssmExePath.normalize().toAbsolutePath().toString(), serviceName);
    }

    /**
     * Returns the Path to an instance of nssm.exe.
     * If required the file is extracted from the Java resources.
     */
    private static Path getNssmExePath() {
        if (nssmExe == null) {
            try {
                nssmExe = extractNssmExe();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return nssmExe;
    }

    /**
     * Extract a copy of nssm.exe from the Java resources to a temporary file.
     * The resource is expected to be named nssm/{win32|win64}/nssm.exe (relative to this class)
     */
    private static Path extractNssmExe() throws IOException {
        String nssmExeRsrc = String.format("nssm/%s/nssm.exe", Platform.is64Bit() ? "win64" : "win32");
        Path nssmExe = Files.createTempFile("nssm", ".exe");
        InputStream inputStream = ControlHelper.class.getResource(nssmExeRsrc).openStream();
        FileUtils.copyInputStreamToFile(inputStream, nssmExe.toFile());
        return nssmExe;
    }


    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = String.format("cmd /c \"%s status %s\" | findstr SERVICE_RUNNING\"", controllerCommand, serviceName);
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            throw new ControlException("Failed to get service status", e);
        }
    }

    @Override
    public void stop() throws ControlException {
        ExecResults r = performControllerServiceCommand("stop");
        mapExitCodeToException(r);
    }

    @Override
    public void start() throws ControlException {
        ExecResults r = performControllerServiceCommand("start");
        mapExitCodeToException(r);
    }

    @Override
    public void restart() throws ControlException {
        ExecResults r = performControllerServiceCommand("restart");
        mapExitCodeToException(r);
    }

    @Override
    public Long getPid() throws ControlException {
        return null;
    }

    public void installService(String[] commandLine) throws ControlException {
        ExecResults r = performControllerServiceCommand("install", "\"" + String.join(" ", commandLine) + "\"");
        mapExitCodeToException(r);
    }

    public void uninstallService() throws ControlException {
        ExecResults r = performControllerServiceCommand("remove", "confirm");
        mapExitCodeToException(r);
    }

    @Override
    public boolean isAutostart() throws ControlException {
        ExecResults r = performControllerServiceCommand("get", "start");
        mapExitCodeToException(r);
        return r.getOutputLines().contains("SERVICE_AUTO_START");
    }

    @Override
    public void setAutostart(boolean autoStart) throws ControlException {
        String startType = autoStart ? "SERVICE_AUTO_START" : "SERVICE_DEMAND_START";
        ExecResults r = performControllerServiceCommand("set", "start", startType);
        mapExitCodeToException(r);
    }

    @Override
    public void assertReady() throws ControlException {
        //FIXME we should check if the nssm command exists but if we run it outisde of a shell it opens a GUI
//        ExecResults r = performControllerCommand("help");
//        String version = r.getOutputLines().get(1);
    }

    @Override
    public boolean isInstalled() throws ControlException {
        ExecResults r = performControllerServiceCommand("status");
        return r.getExitValue() == 0;
    }

    @Override
    protected void mapExitCodeToException(ExecResults execResults) throws ControlException {
        if (execResults.getExitValue() == 3) {
            throw new ServiceNotFoundException(String.format("No service named '%s' is configured", getServiceName()));
        }
        super.mapExitCodeToException(execResults);
    }
}


