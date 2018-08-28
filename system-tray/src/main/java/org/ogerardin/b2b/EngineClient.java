package org.ogerardin.b2b;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * A Utility class to help make API calls to the back2back core.
 */
@Slf4j
public class EngineClient {

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String ENGINE_PROPERTIES_FILE = "application.properties";

    private final int serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    public EngineClient(Path homeDir) throws IOException {

        Path propertiesFile = homeDir.resolve(ENGINE_PROPERTIES_FILE).toAbsolutePath().normalize();
        Properties engineProperties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(propertiesFile.toFile())) {
            engineProperties.load(fileInputStream);
        } catch (FileNotFoundException fnfe) {
            log.warn("engine configuration not found: " + propertiesFile);
        }

        String serverPortProperty = engineProperties.getProperty("server.port");
        serverPort = (serverPortProperty == null) ? DEFAULT_SERVER_PORT : Integer.parseInt(serverPortProperty);
        log.info("server port = " + serverPort);
    }

    public String apiStatus()  {
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
        String url = String.format("%s/api/%s", getBaseUrl(), subUrl);
        return restTemplate.getForObject(url, String.class);
    }

    public String getBaseUrl() {
        return String.format("http://localhost:%d", serverPort);
    }


}
