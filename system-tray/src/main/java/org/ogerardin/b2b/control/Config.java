package org.ogerardin.b2b.control;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config extends CompositeConfiguration {

    private static final String WINDOWS_SERVICE_NAME = "back2back";
    private static final String MAC_JOB_NAME = "back2back";
    private static final String CORE_JAR = "back2back-bundle-repackaged.jar";

    private static final Config CONFIG = new Config();

    private Config() {
        try {
            addConfiguration(new SystemConfiguration());
            addConfiguration(new PropertiesConfiguration("control.properties"));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getHomeDirectory() {
        String dir = CONFIG.getString("back2back.home", ".");
        return Paths.get(dir);
    }

    public static Path getNssmHomeDirectory() {
        String dir = CONFIG.getString("nssm.home", "nssm");
        return Paths.get(dir);
    }

    public static String getCoreJar() {
        return CONFIG.getString("back2back.core.jar", CORE_JAR);
    }

    public static String getWindowsServiceName() {
        return CONFIG.getString("back2back.windows.service.name", WINDOWS_SERVICE_NAME);
    }

    public static String getMacServiceName() {
        return CONFIG.getString("back2back.mac.service.name", MAC_JOB_NAME);
    }

}
