package org.ogerardin.b2b.system_tray.processcontrol;

import com.sun.jna.Platform;
import org.junit.Before;
import org.junit.Rule;
import org.ogerardin.test.PlatformFilter;

public class LaunchctlControllerTest extends ProcessControllerTest {

    @Rule
    public PlatformFilter rule = new PlatformFilter(Platform.MAC);


    private static final String SERVICE_NAME = "bla";

    @Before
    public void setup() {
        this.controller = getController();
    }

    private ProcessController getController() {
        return new MacLaunchctlDaemonController(SERVICE_NAME);
    }
}