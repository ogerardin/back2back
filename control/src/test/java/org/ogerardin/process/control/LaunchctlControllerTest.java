package org.ogerardin.process.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

@EnabledOnOs(OS.MAC)
@Disabled
public class LaunchctlControllerTest extends ProcessControllerTest {

    private static final String SERVICE_NAME = LaunchctlControllerTest.class.getSimpleName();;

    private static MacLaunchctlDaemonController LAUNCHCTL_CONTROLLER = new MacLaunchctlDaemonController(SERVICE_NAME);


    @BeforeEach
    public void setup() {
        this.controller = LAUNCHCTL_CONTROLLER;
    }

}