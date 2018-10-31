package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.LatestStoredRevision;
import org.ogerardin.b2b.domain.mongorepository.LatestStoredRevisionRepository;
import org.ogerardin.b2b.storage.EncryptionException;
import org.ogerardin.b2b.util.CipherHelper;
import org.ogerardin.b2b.util.FormattingHelper;
import org.ogerardin.b2b.web.InputStreamFileResource;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * ItemWriter implementation that uploads the file designated by the input {@link LocalFileInfo} to a remote
 * back2back peer instance using the REST API.
 * Files can be encrypted before transmission if a {@link Key} is provided.
 * Also stores locally the MD5 hash of uploaded files in a {@link LatestStoredRevisionRepository} to allow change detection.
 */
@Slf4j
class PeerItemWriter implements ItemWriter<LocalFileInfo> {

    /** Encyption key. If null, files are sent unencrypted */
    private final Key key;

    /** repository to store the hash and ozher info of remotely stored files */
    private final LatestStoredRevisionProvider peerRevisionInfoRepository;

    /** URL of the peer instance */
    private final URL url;

    @Autowired
    ConfigManager configManager;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    PeerItemWriter(@NonNull LatestStoredRevisionProvider remoteFileVersionInfoRepository,
                   @NonNull String targetHostname, int targetPort, Key key) throws MalformedURLException {

        this.peerRevisionInfoRepository = remoteFileVersionInfoRepository;
        this.key = key;

        // construct URL of remote "peer" API
        this.url = new URL("http", targetHostname, targetPort, "/api/peer/upload");
    }

    PeerItemWriter(LatestStoredRevisionProvider peerRevisionInfoRepository,
                   String targetHostname, int targetPort) throws MalformedURLException {
        this(peerRevisionInfoRepository, targetHostname, targetPort, null);
    }


    @Override
    public void write(List<? extends LocalFileInfo> items) throws IOException, NoSuchAlgorithmException, EncryptionException {
        log.debug("Writing " + Arrays.toString(items.toArray()));

        for (LocalFileInfo item : items) {
            Path path = item.getPath();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (
                // the original file's InputStream
                InputStream fileInputStream = Files.newInputStream(path);
                // the same stream, but also updates a hasher as it is read
                InputStream digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
                // InputStream that will be uploaded.
                // If we have a key (encryption) we use it to wrap the stream in an encrypted stream,
                // otherwise we just use the previous one
                InputStream uploadInputStream = (key != null) ? getCipherInputStream(digestInputStream, key) : digestInputStream;
            ) {
                log.debug("Trying to upload {}", path);
                upload(uploadInputStream, path.toString());
            }

            byte[] md5Hash = messageDigest.digest();
            String hexMd5Hash = FormattingHelper.hex(md5Hash);
            log.debug("Updating local MD5 database for {} -> {}", path, md5Hash);
            val peerRevision = new LatestStoredRevision(path.toString(), hexMd5Hash, false);
            peerRevisionInfoRepository.saveRevisionInfo(peerRevision);

        }
    }

    private static InputStream getCipherInputStream(InputStream is, Key key) throws EncryptionException {
        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        return new CipherInputStream(is, cipher);
    }

    private void upload(InputStream inputStream, String path) {

        //We can't use org.springframework.core.io.InputStreamResource, see https://jira.spring.io/browse/SPR-13571
        Resource resource = new InputStreamFileResource(inputStream, path);

        // build request
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", resource);
//        parts.add("original-path", path);
        parts.add("computer-id", configManager.getMachineInfo().getComputerId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        val requestEntity = new HttpEntity<>(parts, headers);

        // perform HTTP request
        try {
            log.debug("Submitting multipart POST request: {}", requestEntity);
            val responseEntity = REST_TEMPLATE.exchange(
                    url.toURI(),
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            log.debug("Response: {} ", responseEntity);
        } catch (RestClientException | URISyntaxException e) {
            if (log.isDebugEnabled()) {
                log.error("Exception during file upload: ", e);
            }
            else {
                log.error("Exception during file upload: " + e.toString());
            }
            //TODO better error processing
        }
    }

}
