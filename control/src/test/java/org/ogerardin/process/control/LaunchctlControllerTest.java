package org.ogerardin.process.control;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.URISyntaxException;

@EnabledOnOs(OS.MAC)
@Disabled
public class LaunchctlControllerTest extends ProcessControllerTest {

    private static final String SERVICE_NAME = LaunchctlControllerTest.class.getSimpleName();;

    private static MacLaunchctlDaemonController LAUNCHCTL_CONTROLLER = new MacLaunchctlDaemonController(SERVICE_NAME);

    @BeforeAll
    public static void installService() throws ControlException, URISyntaxException {
        String[] command = getServiceCommand();

        LAUNCHCTL_CONTROLLER.installService(command);
    }

    @BeforeEach
    public void setup() {
        this.controller = LAUNCHCTL_CONTROLLER;
    }

    @AfterAll
    public static void uninstallService() throws ControlException {
        LAUNCHCTL_CONTROLLER.uninstallService();
    }

}