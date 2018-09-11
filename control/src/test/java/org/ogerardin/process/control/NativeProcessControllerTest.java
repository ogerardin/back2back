package org.ogerardin.process.control;

import org.junit.jupiter.api.BeforeEach;

import java.net.URISyntaxException;

public class NativeProcessControllerTest extends ProcessControllerTest {

    @BeforeEach
    public void setup() throws URISyntaxException {
        this.controller = getController();
    }

    private ProcessController getController() throws URISyntaxException {
        String[] command = getServiceCommand();

        return NativeProcessController.builder()
                .command(command)
                .pidFileName("test.pid")
                .build();
    }
}