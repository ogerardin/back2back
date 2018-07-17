package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.entity.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.mongorepository.RemoteFileVersionInfoRepository;
import org.ogerardin.b2b.files.md5.StreamingMd5Calculator;
import org.ogerardin.b2b.storage.EncryptionException;
import org.ogerardin.b2b.util.CipherHelper;
import org.ogerardin.b2b.util.FormattingHelper;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * ItemWriter implementation that uploads the file designated by the input {@link LocalFileInfo} to a remote
 * peer instance using the "peer" REST API.
 * Also stores locally the MD5 hash of uploaded files to allow changed detection.
 */
@Slf4j
class PeerItemWriter implements ItemWriter<LocalFileInfo> {

    private final Key key;

    private RemoteFileVersionInfoRepository peerFileVersionInfoRepository;

    @Autowired
    @Qualifier("springMD5Calculator")
    StreamingMd5Calculator md5Calculator;

    @Autowired
    ConfigManager configManager;
    private final URL url;

    PeerItemWriter(@NonNull RemoteFileVersionInfoRepository peerFileVersionInfoRepository,
                   @NonNull String targetHostname, int targetPort, Key key) throws MalformedURLException {

        this.peerFileVersionInfoRepository = peerFileVersionInfoRepository;
        this.key = key;

        // construct URL of remote "peer" API
        this.url = new URL("http", targetHostname, targetPort, "/api/peer/upload");
    }

    PeerItemWriter(RemoteFileVersionInfoRepository peerFileVersionInfoRepository,
                   String targetHostname, int targetPort) throws MalformedURLException {
        this(peerFileVersionInfoRepository, targetHostname, targetPort, null);
    }


    @Override
    public void write(List<? extends LocalFileInfo> items) throws Exception {
        log.debug("Writing " + Arrays.toString(items.toArray()));

        for (LocalFileInfo item : items) {
            Path path = item.getPath();
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (
                    InputStream is = Files.newInputStream(path);
                    DigestInputStream dis = new DigestInputStream(is, md);
                    InputStream uploadInputStream = (key != null) ? getCipherInputStream(dis, key) : dis;
            ) {
                Resource resource = new InputStreamResource(uploadInputStream);
                uploadFile(path, resource);
            }

            // upload successful: update local MD5 database
            byte[] md5Hash = md.digest();
            String hexMd5Hash = FormattingHelper.hex(md5Hash);
            val peerFileVersion = new StoredFileVersionInfo(path.toString(), hexMd5Hash, false);
            peerFileVersionInfoRepository.save(peerFileVersion);
        }

    }

    private static InputStream getCipherInputStream(DigestInputStream dis, Key key) throws EncryptionException, IOException {
        InputStream uploadInputStream;Cipher cipher = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        try (
                CipherInputStream cis = new CipherInputStream(dis, cipher);
        ) {
            uploadInputStream = cis;
        }
        return uploadInputStream;
    }

    private void uploadFile(@NonNull Path path, Resource resource) throws IOException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();


        // build request
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", resource);
        map.add("original-path", path.toString());
        map.add("computer-id", configManager.getMachineInfo().getComputerId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        val requestEntity = new HttpEntity<>(map, headers);

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
            if (log.isDebugEnabled()) {
                log.error("Exception during file upload: ", e);
            }
            else {
                log.error("Exception during file upload: " + e.toString());
            }
            return;
        }
        log.debug("Result of upload: " + result);
    }

}
