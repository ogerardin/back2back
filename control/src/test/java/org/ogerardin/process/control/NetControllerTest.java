package org.ogerardin.process.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

@EnabledOnOs(OS.WINDOWS)
public class NetControllerTest extends ProcessControllerTest {

    private static final String SERVICE_NAME = NetControllerTest.class.getSimpleName();;

    @BeforeEach
    public void setup() {
        this.controller = getController();
    }

    private ProcessController getController() {
        return new WindowsNetServiceController(SERVICE_NAME);
    }
}