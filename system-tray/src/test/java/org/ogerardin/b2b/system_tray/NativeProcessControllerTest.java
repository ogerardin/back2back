package org.ogerardin.b2b.system_tray;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class NativeProcessControllerTest {

    @Test
    public void start() throws IOException {

        NativeProcessController controller = new NativeProcessController(
                new String[] {"notepad"},
                Paths.get("."),
                null
                );

        controller.start();
    }
}