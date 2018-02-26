package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.val;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.PeerFileVersion;
import org.ogerardin.b2b.domain.mongorepository.PeerFileVersionRepository;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.PathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * ItemWriter implementation that uploads the file designated by the input {@link FileInfo} to a remote
 * peer instance using the "peer" REST API.
 * Also stores locally the MD5 hash of uploaded files to allow changed detection.
 */
class PeerItemWriter implements ItemWriter<FileInfo> {

    private static final Log logger = LogFactory.getLog(PeerItemWriter.class);

    private final String targetHostname;
    private final int targetPort;

    private PeerFileVersionRepository peerFileVersionRepository;

    @Autowired
    @Qualifier("springMD5Calculator")
    MD5Calculator md5Calculator;

    @Autowired
    ConfigManager configManager;

    PeerItemWriter(@NonNull PeerFileVersionRepository peerFileVersionRepository,
                   @NonNull String targetHostname, int targetPort) {
        this.peerFileVersionRepository = peerFileVersionRepository;
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

    private void uploadFile(@NonNull Path path) throws IOException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();

        // construct URL of remote "peer" API
        URL url = new URL("http", this.targetHostname, this.targetPort, "/api/peer/upload");

        // build request
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new PathResource(path));
        map.add("original-path", path.toString());
        map.add("computer-id", configManager.getMachineInfo().getComputerId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        // perform HTTP request
        logger.debug("Trying to upload " + path);
        ResponseEntity<String> result = restTemplate.exchange(
                url.toURI(),
                HttpMethod.POST,
                requestEntity,
                String.class);
        logger.debug("Result of upload: " + result);

        //if the upload was successful, store the file's MD5 locally
        if (result.getStatusCode() == HttpStatus.OK) {
            byte[] fileBytes = Files.readAllBytes(path);
            String md5hash = md5Calculator.hexMd5Hash(fileBytes);
            val peerFileVersion = new PeerFileVersion(path.toString(), md5hash);
            peerFileVersionRepository.save(peerFileVersion);
        }
    }


}
