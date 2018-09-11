package org.ogerardin.process.control;

import nop.Nop;
import org.junit.jupiter.api.BeforeEach;
import org.ogerardin.process.execute.JavaCommandLine;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NativeProcessControllerTest extends ProcessControllerTest {

    @BeforeEach
    public void setup() throws URISyntaxException {
        this.controller = getController();
    }

    private ProcessController getController() throws URISyntaxException {
        // get the path of the class file
        // NOTE: class cannot be in default package, or we couldn't access its class file with getResource
        Path mainClassFile = Paths.get(Nop.class.getResource("Nop.class").toURI());

        // get the path of the classpath root for this class
        Path classes = mainClassFile
                .getParent() // directory containing class file
                .getParent() // one level up because class is in package "nop"
                ;

        String[] command = JavaCommandLine.builder()
                .className("nop.Nop")
                .classPathItem(classes)
                .build()
                .getCommand();

        return NativeProcessController.builder()
                .command(command)
                .pidFileName("test.pid")
                .build();
    }
}