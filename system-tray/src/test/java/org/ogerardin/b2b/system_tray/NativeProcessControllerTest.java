package org.ogerardin.b2b.system_tray;

import org.junit.Test;

import java.io.IOException;

public class NativeProcessControllerTest {

    @Test
    public void start() throws IOException, InterruptedException {

        NativeProcessController controller = NativeProcessController.builder()
                .commandLine(new String[]{"yes"})
                .pidfile("test.pid")
                .build();

        controller.start();

        boolean running = controller.isRunning();

        Thread.sleep(10000);

        controller.stop();
    }
}