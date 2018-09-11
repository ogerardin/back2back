package org.ogerardin.process.control;

import nop.Nop;
import org.junit.jupiter.api.BeforeEach;
import org.ogerardin.process.execute.JavaCommandLine;

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

        String[] command = JavaCommandLine.builder()
                .className("nop.Nop")
                .classpathItem(".")
                .build()
                .getCommand();

/*
        String[] command = {
                "java",
                "-cp", ".",
                "nop.Nop"
        };
*/

        return NativeProcessController.builder()
                .workDirectory(path)
                .command(command)
                .pidFileName(PIDFILE)
                .build();
    }
}