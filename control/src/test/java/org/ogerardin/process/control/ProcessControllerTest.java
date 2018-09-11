package org.ogerardin.process.control;

import nop.Nop;
import org.junit.jupiter.api.Test;
import org.ogerardin.process.execute.JavaCommandLine;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ProcessControllerTest {

    protected ProcessController controller;

    protected static String[] getServiceCommand() throws URISyntaxException {
        // get the path of the class file
        // NOTE: class cannot be in default package, or we couldn't access its class file with getResource
        Path mainClassFile = Paths.get(Nop.class.getResource("Nop.class").toURI());

        // get the path of the classpath root for this class
        Path classes = mainClassFile
                .getParent() // directory containing class file
                .getParent() // one level up because class is in package "nop"
                ;

        return JavaCommandLine.builder()
                .className("nop.Nop")
                .classPathItem(classes)
                .build()
                .getCommand();
    }

    @Test
    public void startStop() throws ControlException {
        controller.start();
        assertTrue(controller.isRunning());
        System.out.println("process running, pid=" + controller.getPid());

        controller.stop();
        assertFalse(controller.isRunning());
        System.out.println("process stopped");

        Path pidFile = ((NativeProcessController) controller).getPidFile();
        assertFalse(Files.exists(pidFile));

    }

    @Test
    public void startStart() throws ControlException {
        controller.start();
        assertTrue(controller.isRunning());
        System.out.println("process running, pid=" + controller.getPid());

        try {
            controller.start();
            fail("already started");
        } catch (ControlException ignored) {
        }

        controller.stop();
    }

    @Test
    public void startRestartStop() throws ControlException {
        controller.start();
        assertTrue(controller.isRunning());
        long pid0 = controller.getPid();
        System.out.println("process started, pid=" + pid0);

        controller.restart();
        assertTrue(controller.isRunning());
        long pid1 = controller.getPid();
        System.out.println("process restarted, pid=" + pid1);
        assertNotEquals(pid0, pid1);

        controller.stop();
        assertFalse(controller.isRunning());
        System.out.println("process stopped");
    }

}
