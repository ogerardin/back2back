package org.ogerardin.processcontrol;

import com.sun.jna.Platform;
import nop.Nop;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.MissingResourceException;

@EnabledOnOs(OS.WINDOWS)
public class NssmControllerTest extends ProcessControllerTest {

    private static final String SERVICE_NAME = NssmControllerTest.class.getSimpleName();

    private static WindowsNssmServiceController NSSM_CONTROLLER
            = new WindowsNssmServiceController(getNssmPath().toAbsolutePath().toString(), SERVICE_NAME);

    private static Path getNssmPath() {
        // NSSM is downloaded by Maven during "generate-test-resources" and placed into
        // target/test-classes/org/ogerardin/processcontrol  so that it is available as a resource of this class.
        // If running this test outside of Maven, run "mvn generate-test-resources" before.
        String nssmRsrcPath = Platform.is64Bit() ? "nssm-2.24/win64/nssm.exe" : "nssm-2.24/win32/nssm.exe";

        try {
            URL nssmRsrcUrl = WindowsNssmServiceController.class.getResource(nssmRsrcPath);
            if (nssmRsrcUrl == null) {
                throw new MissingResourceException("Missing NSSM executable: " + nssmRsrcPath, WindowsNssmServiceController.class.getName(), nssmRsrcPath);
            }
            return Paths.get(nssmRsrcUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
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
                "-cp", path.toAbsolutePath().toString(),
                "nop.Nop"
        });
    }

    @AfterAll
    public static void uninstallService() throws ControlException {
        NSSM_CONTROLLER.uninstallService();
    }


    @BeforeEach
    public void setup() throws ControlException {
        this.controller = NSSM_CONTROLLER;
    }

}