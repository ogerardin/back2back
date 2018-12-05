package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.domain.mongorepository.FileBackupStatusInfoRepository;
import org.ogerardin.b2b.storage.EncryptionException;
import org.ogerardin.b2b.util.CipherHelper;
import org.ogerardin.b2b.web.InputStreamFileResource;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import java.security.Key;
import java.util.Arrays;
import java.util.List;

/**
 * ItemWriter implementation that uploads the file designated by the input {@link LocalFileInfo} to a remote
 * back2back peer instance using the REST API.
 * Files can be encrypted before transmission if a {@link Key} is provided.
 * Also stores locally the MD5 hash of uploaded files in a {@link FileBackupStatusInfoRepository} to allow change detection.
 */
@Slf4j
class PeerItemWriter implements ItemWriter<LocalFileInfo> {

    /** Encyption key. If null, files are sent unencrypted */
    private final Key key;

    /** repository to store the hash and ozher info of backed up files */
    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    /** URL of the peer instance */
    private final URL url;

    @Autowired
    ConfigManager configManager;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    PeerItemWriter(@NonNull FileBackupStatusInfoProvider fileBackupStatusInfoProvider,
                   @NonNull String targetHostname, int targetPort, Key key) throws MalformedURLException {

        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
        this.key = key;

        // construct URL of remote "peer" API
        this.url = new URL("http", targetHostname, targetPort, "/api/peer/upload");
    }

    PeerItemWriter(FileBackupStatusInfoProvider fileBackupStatusInfoProvider,
                   String targetHostname, int targetPort) throws MalformedURLException {
        this(fileBackupStatusInfoProvider, targetHostname, targetPort, null);
    }


    @Override
    public void write(@NonNull List<? extends LocalFileInfo> items) throws IOException, EncryptionException {
        log.debug("Writing " + Arrays.toString(items.toArray()));

        for (LocalFileInfo item : items) {
            Path path = item.getPath();
            try (
                InputStream fileInputStream = Files.newInputStream(path);
                // If we have a key (encryption) we use it to wrap the stream in an encrypted stream,
                // otherwise we just use the previous one
                InputStream uploadInputStream = (key != null) ? getCipherInputStream(fileInputStream, key) : fileInputStream;
            ) {
                log.debug("Trying to upload {}", path);
                upload(uploadInputStream, path.toString());
            }

            // update stored hash
            log.debug("Updating local hash for {} -> {}", path, item.getHashes());
            val peerRevision = new FileBackupStatusInfo(path.toString(), item.getHashes());
            fileBackupStatusInfoProvider.saveRevisionInfo(peerRevision);
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
