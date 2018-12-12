package org.ogerardin.b2b.storage.peer;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.storage.*;
import org.ogerardin.b2b.util.CipherHelper;
import org.ogerardin.b2b.util.NotImplementedException;
import org.ogerardin.b2b.web.InputStreamFileResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Key;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * An implementation of {@link StorageService} using the remote peer REST API
 *
 * @see org.ogerardin.b2b.api.PeerController
 */
@Data
@Slf4j
public class PeerStorageService implements StorageService {

    /**
     * URL of the peer instance
     */
    private final URL baseUrl;

    private final String computerId;

    private final RestTemplate restTemplate;

    public PeerStorageService(String targetHostname, int targetPort, String computerId, RestTemplate restTemplate) throws MalformedURLException {
        // construct URL of remote "peer" API
        this.baseUrl = new URL("http", targetHostname, targetPort, "/api/peer/");
        this.computerId = computerId;
        this.restTemplate = restTemplate;
    }

    @Override
    public void init() {
    }

    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {
        try {
            URL url = new URL(baseUrl, String.format("list?computer-id=%s", computerId));
            log.info("Listing from URL {}", url);
            ResponseEntity<FileInfo[]> responseEntity = restTemplate.getForEntity(url.toURI(), FileInfo[].class);
            assertResponseSuccessful(responseEntity);
            return Arrays.stream(responseEntity.getBody());
        } catch (URISyntaxException | IOException e) {
            throw new StorageException("Failed to list files", e);
        }
    }

    @Override
    public Stream<RevisionInfo> getAllRevisions() {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getAsInputStream(String filename) throws FileNotFoundException {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getAsInputStream(String filename, Key key) throws FileNotFoundException, EncryptionException {
        throw new NotImplementedException();
    }

    @Override
    public String store(InputStream inputStream, String filename) {
        log.debug("Trying to upload {}", filename);
        try {
            return upload(inputStream, filename);
        } catch (URISyntaxException | IOException e) {
            throw new StorageException("Failed to upload file as " + filename, e);
        }
    }

    @Override
    public String store(InputStream inputStream, String filename, @NonNull Key key) throws EncryptionException {
        try (
                // wrap the stream in an encrypted stream
                InputStream uploadInputStream = getCipherInputStream(inputStream, key);
        ) {
            log.debug("Trying to upload {}", filename);
            return upload(uploadInputStream, filename);
        } catch (IOException | URISyntaxException e) {
            throw new StorageException("Failed to upload encrypted file as " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException();
    }

    @Override
    public RevisionInfo[] getRevisions(String filename) {
        throw new NotImplementedException();
    }

    @Override
    public RevisionInfo getLatestRevision(String filename) {
        throw new NotImplementedException();
    }

    @Override
    public RevisionInfo getRevisionInfo(String revisionId) {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId) {
        try {
            return download(revisionId);
        } catch (URISyntaxException | IOException e) {
            throw new StorageException("Failed to download revision " + revisionId, e);
        }
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId, Key key) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(String filename) {
        throw new NotImplementedException();
    }

    private static InputStream getCipherInputStream(InputStream is, Key key) throws EncryptionException {
        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        return new CipherInputStream(is, cipher);
    }

    private String upload(InputStream inputStream, String path) throws URISyntaxException, IOException {
        //We can't use org.springframework.core.io.InputStreamResource, see https://jira.spring.io/browse/SPR-13571
        Resource resource = new InputStreamFileResource(inputStream, path);

        // build request
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", resource);
//        parts.add("original-path", path);
        parts.add("computer-id", computerId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        val requestEntity = new HttpEntity<>(parts, headers);

        // perform HTTP request
        URL uploadUrl = new URL(baseUrl, "upload");
        log.debug("Submitting multipart POST request: {}", requestEntity);
        val responseEntity = restTemplate.exchange(
                uploadUrl.toURI(),
                HttpMethod.POST,
                requestEntity,
                String.class);
        log.debug("Response: {} ", responseEntity);

        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Expected status code 2xx, got: " + responseEntity);
        }

        // the response is expected to contain the new revisionId as body (String)
        return responseEntity.getBody();
    }

    private InputStream download(String revisionId) throws IOException, URISyntaxException {
        URL downloadUrl = new URL(baseUrl, String.format("download/%s?computer-id=%s", revisionId, computerId));
        log.info("Downloading from URL {}", downloadUrl);
        ResponseEntity<Resource> responseEntity = restTemplate.getForEntity(downloadUrl.toURI(), Resource.class);
        assertResponseSuccessful(responseEntity);

        return responseEntity.getBody().getInputStream();
    }

    private void assertResponseSuccessful(ResponseEntity<?> responseEntity) throws IOException {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Expected status code 2xx, got: " + responseEntity);
        }
    }

}
