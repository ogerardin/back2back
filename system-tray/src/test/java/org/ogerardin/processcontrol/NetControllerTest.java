package org.ogerardin.processcontrol;

import com.sun.jna.Platform;
import org.junit.Before;
import org.junit.Rule;
import org.ogerardin.test.PlatformFilter;

public class NetControllerTest extends ProcessControllerTest {

    @Rule
    public PlatformFilter rule = new PlatformFilter(Platform.WINDOWS);


    private static final String SERVICE_NAME = "bla";

    @Before
    public void setup() {
        this.controller = getController();
    }

    private ProcessController getController() {
        return new WindowsNetServiceController(SERVICE_NAME);
    }
}