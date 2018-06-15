package org.ogerardin.b2b.system_tray.processcontrol;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.IOException;

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

    public WindowsNssmServiceController(String controller, String serviceName) {
        super(controller, serviceName);
    }

    @Override
    public String getControllerInfo() throws ControlException {
        ExecResults r = performControllerCommand("help");
        return r.getOutputLines().get(1);
    }

    @Override
    public boolean isRunning() throws ControlException {
        try {
            //findstr exit code 0 if found, 1 if it doesn't
            String cmd = String.format("cmd /c \"%s status %s\" | findstr SERVICE_RUNNING\"", controller, serviceName);
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
        failIfNonZeroExitCode(r);
    }

    @Override
    public void start() throws ControlException {
        ExecResults r = performControllerServiceCommand("start");
        failIfNonZeroExitCode(r);
    }

    @Override
    public void restart() throws ControlException {
        ExecResults r = performControllerServiceCommand("restart");
        failIfNonZeroExitCode(r);
    }

    @Override
    public Long getPid() throws ControlException {
        return null;
    }

    public void installService(String[] commandLine) throws ControlException {
        ExecResults r = performControllerServiceCommand("install", "\"" + String.join(" ", commandLine) + "\"");
        failIfNonZeroExitCode(r);
    }

    public void uninstallService() throws ControlException {
        ExecResults r = performControllerServiceCommand("remove", "confirm");
        failIfNonZeroExitCode(r);
    }

    @Override
    public boolean isAutostart() throws ControlException {
        ExecResults r = performControllerServiceCommand("get", "start");
        failIfNonZeroExitCode(r);
        return r.getOutputLines().contains("SERVICE_AUTO_START");
    }

    @Override
    public void setAutostart(boolean autoStart) throws ControlException {
        String startType = autoStart ? "SERVICE_AUTO_START" : "SERVICE_DEMAND_START";
        ExecResults r = performControllerServiceCommand("set", "start", startType);
        failIfNonZeroExitCode(r);
    }
}


