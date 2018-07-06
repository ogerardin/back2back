package org.ogerardin.processcontrol;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.ogerardin.process.ProcessExecutor.ExecResults;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Controller using "nssm" (https://nssm.cc/) to control a Windows service.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class WindowsNssmServiceController extends ExternalServiceController implements ServiceController {

    public WindowsNssmServiceController(String serviceName) {
        this("nssm", serviceName);
    }

    public WindowsNssmServiceController(String nssmCommand, String serviceName) {
        super(nssmCommand, serviceName);
    }

    public WindowsNssmServiceController(Path nssmExePath, String serviceName) {
        super(nssmExePath.normalize().toAbsolutePath().toString(), serviceName);
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


