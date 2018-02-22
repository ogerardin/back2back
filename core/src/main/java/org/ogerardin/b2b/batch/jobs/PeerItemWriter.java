package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.PathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 */
class PeerItemWriter implements ItemWriter<FileInfo> {

    private static final Log logger = LogFactory.getLog(PeerItemWriter.class);

    private final String targetHostname;
    private final int targetPort;

    PeerItemWriter(@NotNull String targetHostname, int targetPort) {
        this.targetHostname = targetHostname;
        this.targetPort = targetPort;
    }

    @Override
    public void write(List<? extends FileInfo> items) throws Exception {
        logger.debug("Writing " + Arrays.toString(items.toArray()));

        for (FileInfo item : items) {
            uploadFile(item.getPath());
        }

    }

    private void uploadFile(Path path) throws MalformedURLException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();

        URL url = new URL("http", this.targetHostname, this.targetPort, "/api/peer/upload");

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new PathResource(path));
        map.add("original-path", path.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        logger.debug("Trying to upload " + path);

        ResponseEntity<String> result = restTemplate.exchange(
                url.toURI(),
                HttpMethod.POST,
                requestEntity,
                String.class);

        logger.debug("Result of upload: " + result);
    }
}
