package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.mongorepository.PeerFileVersionInfoRepository;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.PathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * ItemWriter implementation that uploads the file designated by the input {@link LocalFileInfo} to a remote
 * peer instance using the "peer" REST API.
 * Also stores locally the MD5 hash of uploaded files to allow changed detection.
 */
@Slf4j
class PeerItemWriter implements ItemWriter<LocalFileInfo> {

    private final String targetHostname;
    private final int targetPort;

    private PeerFileVersionInfoRepository peerFileVersionInfoRepository;

    @Autowired
    @Qualifier("springMD5Calculator")
    MD5Calculator md5Calculator;

    @Autowired
    ConfigManager configManager;

    PeerItemWriter(@NonNull PeerFileVersionInfoRepository peerFileVersionInfoRepository,
                   @NonNull String targetHostname, int targetPort) {
        this.peerFileVersionInfoRepository = peerFileVersionInfoRepository;
        this.targetHostname = targetHostname;
        this.targetPort = targetPort;

    }

    @Override
    public void write(List<? extends LocalFileInfo> items) throws Exception {
        log.debug("Writing " + Arrays.toString(items.toArray()));

        for (LocalFileInfo item : items) {
            uploadFile(item.getPath());
        }

    }

    //FIXME uploading a zero-bytes file generates an error 400
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
        log.debug("Trying to upload " + path);
        ResponseEntity<String> result;
        try {
            result = restTemplate.exchange(
                    url.toURI(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
        } catch (RestClientException e) {
            log.error("Exception during file upload: ", e);
            return;
        }
        log.debug("Result of upload: " + result);

        //if the upload was successful, store the file's MD5 locally
        if (result.getStatusCode() == HttpStatus.OK) {
            byte[] fileBytes = Files.readAllBytes(path);
            String md5hash = md5Calculator.hexMd5Hash(fileBytes);
            val peerFileVersion = new StoredFileVersionInfo(path.toString(), md5hash);
            peerFileVersionInfoRepository.save(peerFileVersion);
        }
    }


}
