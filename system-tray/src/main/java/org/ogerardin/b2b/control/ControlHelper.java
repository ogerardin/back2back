package org.ogerardin.b2b.control;

import com.sun.jna.Platform;
import org.ogerardin.processcontrol.ControlException;
import org.ogerardin.processcontrol.MacLaunchctlDaemonController;
import org.ogerardin.processcontrol.ServiceController;
import org.ogerardin.processcontrol.WindowsNssmServiceController;

import java.nio.file.Path;

public class ControlHelper {
    public static ServiceController getPlatformServiceController() throws ControlException {
        ServiceController serviceController;
        switch (Platform.getOSType()) {
            case Platform.WINDOWS: {
                Path nssmExe = getNssmExePath();
                serviceController = new WindowsNssmServiceController(nssmExe, Config.getWindowsServiceName());
                break;
            }
            case Platform.MAC: {
                serviceController = new MacLaunchctlDaemonController(Config.getMacServiceName());
                break;
            }
            default: {
                throw new ControlException("No service controller available for platform " + System.getProperty("os.name"));
            }
        }

        serviceController.assertReady();
        return serviceController;
    }

    private static Path getNssmExePath() {
        return Platform.is64Bit() ?
                Config.getNssmHomeDirectory().resolve("win64").resolve("nssm")
                : Config.getNssmHomeDirectory().resolve("win32").resolve("nssm");
    }
}
