package org.ogerardin.process.control;

import com.sun.jna.Platform;
import org.ogerardin.process.execute.JavaCommandLine;

import java.nio.file.Path;


public enum ControlHelper {
    ;


    public static ServiceController getPlatformServiceController(String serviceName) throws ControlException {
        ServiceController serviceController;
        switch (Platform.getOSType()) {
            case Platform.WINDOWS: {
                serviceController = new WindowsNssmServiceController(serviceName);
                break;
            }
            case Platform.MAC: {
                serviceController = new MacLaunchctlDaemonController(serviceName);
                break;
            }
            default: {
                throw new ControlException("No service controller available for platform " + System.getProperty("os.name"));
            }
        }

        return serviceController;
    }

    public static NativeProcessController getJarProcessController(Path jarFile) {
        String[] command = JavaCommandLine.builder()
                .jarFile(jarFile)
                .build()
                .getCommand();

        return NativeProcessController.builder()
                .command(command)
                .build();
    }

}
