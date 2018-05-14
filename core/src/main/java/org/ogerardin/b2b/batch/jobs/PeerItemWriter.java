package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.mongorepository.RemoteFileVersionInfoRepository;
import org.ogerardin.b2b.files.md5.StreamingMd5Calculator;
import org.ogerardin.b2b.util.CipherHelper;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.FileInputStream;
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
            if (key != null) {
                MessageDigest md = MessageDigest.getInstance("MD5");
                Cipher cipher = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
                try (
                        InputStream is = Files.newInputStream(item.getPath());
                        DigestInputStream dis = new DigestInputStream(is, md);
                        CipherInputStream cis = new CipherInputStream(dis, cipher);


                ) {
                    InputStreamResource isr = new InputStreamResource(cis);
                    uploadFile(item.getPath(), isr);
                }
                byte[] md5 = md.digest();
            }
            else {
                PathResource resource = new PathResource(item.getPath());
                uploadFile(item.getPath(), resource);
            }
        }

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

        //if the upload was successful, store the file's MD5 in the local repository
        if (result.getStatusCode() == HttpStatus.OK) {
            try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
                String md5hash = md5Calculator.hexMd5Hash(fileInputStream);
                val peerFileVersion = new StoredFileVersionInfo(path.toString(), md5hash, false);
                peerFileVersionInfoRepository.save(peerFileVersion);
            }
        }
    }


}
