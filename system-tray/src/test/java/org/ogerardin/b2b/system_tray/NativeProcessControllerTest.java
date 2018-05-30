package org.ogerardin.b2b.system_tray;

import nop.Nop;
import org.junit.Assert;
import org.junit.Test;
import org.ogerardin.b2b.system_tray.processcontrol.ControlException;
import org.ogerardin.b2b.system_tray.processcontrol.NativeProcessController;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NativeProcessControllerTest {

    public static final String PIDFILE = "test.pid";

    @Test
    public void startStop() throws URISyntaxException, ControlException, FileNotFoundException {
        NativeProcessController controller = getController();

        controller.start();
        Assert.assertTrue(controller.isRunning());
        System.out.println("process started, pid=" + controller.readPid());

        controller.stop();
        Assert.assertFalse(controller.isRunning());
        System.out.println("process stopped");
    }

    @Test
    public void startStart() throws URISyntaxException, ControlException, FileNotFoundException {
        NativeProcessController controller = getController();

        controller.start();
        Assert.assertTrue(controller.isRunning());
        System.out.println("process started, pid=" + controller.readPid());

        try {
            controller.start();
            Assert.fail("already started");
        } catch (ControlException ignored) {
        }

        controller.stop();
    }

    @Test
    public void startRestartStop() throws URISyntaxException, ControlException, FileNotFoundException {

        NativeProcessController controller = getController();

        controller.start();
        Assert.assertTrue(controller.isRunning());
        long pid0 = controller.readPid();
        System.out.println("process started, pid=" + pid0);

        controller.restart();
        Assert.assertTrue(controller.isRunning());
        long pid1 = controller.readPid();
        System.out.println("process restarted, pid=" + pid1);
        Assert.assertNotEquals(pid0, pid1);

        controller.stop();
        Assert.assertFalse(controller.isRunning());
        System.out.println("process stopped");
    }

    @Test
    public void stop() throws URISyntaxException, ControlException {
        NativeProcessController controller = getController();

        Assert.assertFalse(controller.isRunning());
        controller.stop();

        Assert.assertFalse(Files.exists(controller.getPidFile()));
    }

    private NativeProcessController getController() throws URISyntaxException {
        // get the path of the class file (as  a URL)
        URL url = Nop.class.getResource("Nop.class");
        // get the path of the classpath root for this class
        Path path = Paths.get(url.toURI())
                .getParent() // directory of class file
                .getParent() // one level up because class is in package "nop"
                ;

        return NativeProcessController.builder()
                .homeDirectory(path)
                .commandLine(new String[]{
                        "java",
                        "-cp",
                        ".",
                        "nop.Nop"
                })
                .pidfile(PIDFILE)
                .build();
    }
}