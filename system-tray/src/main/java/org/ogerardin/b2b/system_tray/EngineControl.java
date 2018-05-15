package org.ogerardin.b2b.system_tray;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class EngineControl {

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String ENGINE_PROPERTIES_FILE = "application.properties";

    private final int serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    public EngineControl(Path installDir) throws IOException {

        Path propertiesFile = installDir.resolve(ENGINE_PROPERTIES_FILE).toAbsolutePath().normalize();
        Properties coreProperties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(propertiesFile.toFile())) {
            coreProperties.load(fileInputStream);
        } catch (FileNotFoundException fnfe) {
            log.warn("engine configuration not found: " + propertiesFile);
        }

        String serverPortProperty = coreProperties.getProperty("server.port");
        if (serverPortProperty == null) {
            serverPort = DEFAULT_SERVER_PORT;
        }
        else {
            serverPort = Integer.parseInt(serverPortProperty);
        }
        log.info("server port = " + serverPort);
    }

    public String engineStatus()  {
        return apiCall("app/status");
    }

    public String version() {
        return apiCall("app/version");
    }

    public String restart() {
        return apiCall("app/restart");
    }

    public String shutdown() {
        return apiCall("app/shutdown");
    }
    private String apiCall(String subUrl) throws RestClientException {
        String url = String.format("http://localhost:%d/api/%s", serverPort, subUrl);
        return restTemplate.getForObject(url, String.class);
    }




}
