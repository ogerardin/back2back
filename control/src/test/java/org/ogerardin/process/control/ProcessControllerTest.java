package org.ogerardin.process.control;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public abstract class ProcessControllerTest {

    protected ProcessController controller;

    @Test
    public void startStop() throws ControlException {
        controller.start();
        assertTrue(controller.isRunning());
        System.out.println("process started, pid=" + controller.getPid());

        controller.stop();
        assertFalse(controller.isRunning());
        System.out.println("process stopped");
    }

    @Test
    public void startStart() throws ControlException {
        controller.start();
        assertTrue(controller.isRunning());
        System.out.println("process started, pid=" + controller.getPid());

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

    @Test
    public void stop() throws ControlException {
        assertFalse(controller.isRunning());
        controller.stop();

        Path pidFile = ((NativeProcessController) controller).getPidFile();
        assertFalse(Files.exists(pidFile));
    }
}
