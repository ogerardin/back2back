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
import org.ogerardin.b2b.web.InputStreamFileResource;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
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
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (
                // the original file's InputStream
                InputStream fileInputStream = Files.newInputStream(path);
                // the same stream, but also updates the digest as it is read
                InputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
                // InputStream that will be uploaded
                // if we have a key (encryption) we use wrap the stream in an ecrypted stream,
                // otherwise we just use the previous one
                InputStream uploadInputStream = (key != null) ? getCipherInputStream(digestInputStream, key) : digestInputStream;
            ) {
                //FIXME we can't use org.springframework.core.io.InputStreamResource, see https://jira.spring.io/browse/SPR-13571
                Resource resource = new InputStreamFileResource(uploadInputStream, path.toString());

                uploadFile(path, resource);
            }

            // upload successful: update local MD5 database
            byte[] md5Hash = messageDigest.digest();
            String hexMd5Hash = FormattingHelper.hex(md5Hash);
            val peerFileVersion = new StoredFileVersionInfo(path.toString(), hexMd5Hash, false);
            peerFileVersionInfoRepository.save(peerFileVersion);
        }

    }

    private static InputStream getCipherInputStream(InputStream is, Key key) throws EncryptionException {
        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        return new CipherInputStream(is, cipher);
    }

    private void uploadFile(@NonNull Path path, Resource resource) throws URISyntaxException {
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
