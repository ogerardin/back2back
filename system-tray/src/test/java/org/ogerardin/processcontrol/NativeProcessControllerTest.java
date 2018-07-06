package org.ogerardin.processcontrol;

import nop.Nop;
import org.junit.jupiter.api.BeforeEach;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NativeProcessControllerTest extends ProcessControllerTest {

    private static final String PIDFILE = "test.pid";

    @BeforeEach
    public void setup() throws URISyntaxException {
        this.controller = getController();
    }

    private ProcessController getController() throws URISyntaxException {
        // get the path of the class file (as a URL)
        URL url = Nop.class.getResource("Nop.class");
        // get the path of the classpath root for this class
        Path path = Paths.get(url.toURI())
                .getParent() // directory of class file
                .getParent() // one level up because class is in package "nop"
                ;

        return NativeProcessController.builder()
                .workDirectory(path)
                .command(new String[]{
                        "java",
                        "-cp", ".",
                        "nop.Nop"
                })
                .pidFileName(PIDFILE)
                .build();
    }
}