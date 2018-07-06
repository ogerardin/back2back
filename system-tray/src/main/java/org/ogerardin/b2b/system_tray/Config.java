package org.ogerardin.b2b.system_tray;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public class Config {
    public static final String WINDOWS_SERVICE_NAME = "back2back";
    public static final String MAC_JOB_NAME = "back2back";
    private static final String CORE_JAR = "back2back-bundle-repackaged.jar";

    private Path installDir;
    private Path nssmHome;
    private String coreJar;

    public static Config read() {
        Config config = new Config();

        String installDir = System.getProperty("back2back.home", ".");
        config.installDir = Paths.get(installDir);

        String nssmHome = System.getProperty("nssm.home", "nssm");
        config.nssmHome = Paths.get(nssmHome);

        config.coreJar = System.getProperty("back2back.core.jar", CORE_JAR);
        return config;
    }



}
