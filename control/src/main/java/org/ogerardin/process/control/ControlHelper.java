package org.ogerardin.process.control;

import com.sun.jna.Platform;
import org.apache.commons.lang3.ArrayUtils;

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
                throw new ControlException("No service controller available for platform " + System
                        .getProperty("os.name"));
            }
        }

        serviceController.assertReady();
        return serviceController;
    }

    public static NativeProcessController buildJarProcessController(Path jarFile) {
        return NativeProcessController.builder()
                .command(buildJavaCommand(jarFile))
                .build();
    }

    public static String[] buildJavaCommand(Path jarFile, String... extraArgs) {
        String[] cmdArray = {
                "java",
                "-jar", jarFile.toAbsolutePath().toString()
        };
        String[] result = ArrayUtils.addAll(cmdArray, extraArgs);
        return result;
    }
}
