package org.ogerardin.process.control;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.URISyntaxException;

@EnabledOnOs(OS.WINDOWS)
@Disabled // unreliable
public class NssmControllerTest extends ProcessControllerTest {

    private static final String SERVICE_NAME = NssmControllerTest.class.getSimpleName();

    private static WindowsNssmServiceController NSSM_CONTROLLER = new WindowsNssmServiceController(SERVICE_NAME);

    @BeforeAll
    public static void installService() throws ControlException, URISyntaxException {
        String[] command = getServiceCommand();

        NSSM_CONTROLLER.installService(command);
    }

    @BeforeEach
    public void setup() {
        this.controller = NSSM_CONTROLLER;
    }

    @AfterAll
    public static void uninstallService() throws ControlException {
        NSSM_CONTROLLER.uninstallService();
    }

}