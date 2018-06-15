package org.ogerardin.b2b.system_tray.processcontrol;

import com.sun.jna.Platform;
import nop.Nop;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.ogerardin.test.PlatformFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NssmControllerTest extends ProcessControllerTest {

    @Rule
    public PlatformFilter rule = new PlatformFilter(Platform.WINDOWS);

    private static final String SERVICE_NAME = NssmControllerTest.class.getSimpleName();

    private static WindowsNssmServiceController NSSM_CONTROLLER
            = new WindowsNssmServiceController(getNssmPath().toAbsolutePath().toString(), SERVICE_NAME);

    private static Path getNssmPath() {
        URI uri;
        try {
            //FIXME
            uri = WindowsNssmServiceController.class.getResource("nssm-2.24/win64/nssm.exe").toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return Paths.get(uri);
    }


    @BeforeClass
    public static void installService() throws ControlException, URISyntaxException {
        // get the path of the class file (as a URL)
        URL url = Nop.class.getResource("Nop.class");
        // get the path of the classpath root for this class
        Path path = Paths.get(url.toURI())
                .getParent() // directory of class file
                .getParent() // one level up because class is in package "nop"
                ;

        NSSM_CONTROLLER.installService(new String[]{
                "java",
                "-cp",
                path.toAbsolutePath().toString(),
                "nop.Nop"
        });
    }

    @AfterClass
    public static void uninstallService() throws ControlException {
        NSSM_CONTROLLER.uninstallService();
    }


    @Before
    public void setup() throws ControlException {
        this.controller = NSSM_CONTROLLER;
    }

}