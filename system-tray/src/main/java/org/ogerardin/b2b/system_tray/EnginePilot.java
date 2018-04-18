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
public class EnginePilot {

    private static final int DEFAULT_SERVER_PORT = 8080;

    private final int serverPort;

    private final RestTemplate restTemplate = new RestTemplate();

    public EnginePilot(Path installDir) throws IOException {

        Path propertiesFile = installDir.resolve("application.propeties").toAbsolutePath().normalize();
        Properties coreProperties = new Properties();
        try {
            coreProperties.load(new FileInputStream(propertiesFile.toFile()));
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

    public String getEngineStatus() throws RestClientException {
        return apiCall("app/status");
    }

    public String version() {
        return apiCall("app/version");
    }


    private String apiCall(String subUrl) {
        String url = String.format("http://localhost:%d/api/%s", serverPort, subUrl);
        return restTemplate.getForObject(url, String.class);
    }




}
