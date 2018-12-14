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
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Key;
import java.util.Arrays;
import java.util.function.Supplier;
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
    private final URI baseUri;

    private final String computerId;

    private final RestTemplate restTemplate;

    public PeerStorageService(String targetHostname, int targetPort, String computerId, RestTemplate restTemplate) {
        // construct URL of remote "peer" API
        try {
            this.baseUri = new URL("http", targetHostname, targetPort, "/api/peer/").toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new StorageException("Failed to initialize remote URI", e);
        }
        this.computerId = computerId;
        this.restTemplate = restTemplate;
    }

    @Override
    public void init() {
    }

    @Override
    public Stream<FileInfo> getAllFiles(boolean includeDeleted) {
        try {
//            URL url = new URL(baseUri, String.format("list?computer-id=%s", computerId));
            URI uri = UriComponentsBuilder.fromUri(baseUri)
                    .path("list")
                    .queryParam("computer-id", computerId)
                    .encode()
                    .build()
                    .toUri();

            log.info("Listing from {}", uri);
            ResponseEntity<FileInfo[]> responseEntity = restTemplate.getForEntity(uri, FileInfo[].class);
            assertResponseSuccessful(responseEntity);
            return Arrays.stream(responseEntity.getBody());
        } catch (IOException e) {
            throw new StorageException("Failed to list files", e);
        }
    }

    @Override
    public Stream<RevisionInfo> getAllRevisions() {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getAsInputStream(String filename) throws IOException, FileNotFoundException {
        return downloadFile(filename);
    }

    @Override
    public InputStream getAsInputStream(String filename, Key key) throws FileNotFoundException, EncryptionException, IOException {
        InputStream inputStream = downloadFile(filename);
        return getDecryptedInputStream(inputStream, key);
    }

    @Override
    public String store(InputStream inputStream, String filename) throws IOException {
        return upload(inputStream, filename);
    }

    @Override
    public String store(InputStream inputStream, String filename, @NonNull Key key) throws EncryptionException, IOException {
        try (
                // wrap the stream in an encrypted stream
                InputStream uploadInputStream = getCipherInputStream(inputStream, key);
        ) {
            return upload(uploadInputStream, filename);
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
    public InputStream getRevisionAsInputStream(String revisionId) throws RevisionNotFoundException, IOException {
        return downloadRevision(revisionId);
    }

    @Override
    public InputStream getRevisionAsInputStream(String revisionId, Key key) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(String filename) {
        throw new NotImplementedException();
    }

    private static InputStream getCipherInputStream(InputStream inputStream, Key key) throws EncryptionException {
        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.ENCRYPT_MODE);
        return new CipherInputStream(inputStream, cipher);
    }

    private static InputStream getDecryptedInputStream(InputStream inputStream, Key key) throws EncryptionException {
        Cipher cipher = CipherHelper.getAesCipher(key, Cipher.DECRYPT_MODE);
        return new CipherInputStream(inputStream, cipher);
    }

    private String upload(InputStream inputStream, String filename) throws IOException {
        //We can't use org.springframework.core.io.InputStreamResource, see https://jira.spring.io/browse/SPR-13571
        Resource resource = new InputStreamFileResource(inputStream, filename);

        // build request
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", resource);
//        parts.add("computer-id", computerId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        val requestEntity = new HttpEntity<>(parts, headers);

        // perform HTTP request
        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .path("upload")
                .queryParam("computer-id", computerId)
                .encode()
                .build()
                .toUri();
        log.debug("Submitting multipart POST request: {}", requestEntity);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                requestEntity,
                String.class);
        log.debug("Response: {} ", responseEntity);
        assertResponseSuccessful(responseEntity);

        // the response is expected to contain the new revisionId as body (String)
        return responseEntity.getBody();
    }

    private InputStream downloadRevision(String revisionId) throws IOException, RevisionNotFoundException {
        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .path("download/{revisionId}")
                .queryParam("computer-id", computerId)
                .encode()
                .buildAndExpand(revisionId)
                .toUri();
        return getInputStream(uri, () -> new RevisionNotFoundException(revisionId));
    }

    private InputStream downloadFile(String filename) throws IOException, FileNotFoundException {
        URI uri = UriComponentsBuilder.fromUri(baseUri)
                .path("download")
                .queryParam("computer-id", computerId)
                .queryParam("filename", filename)
                .encode()
                .build()
                .toUri();
        return getInputStream(uri, () -> new FileNotFoundException(filename));
    }

    /**
     * Performs a HTTP GET on the specified URL and returns an {@link InputStream} that gives access to the
     * resource contents. If the response has status 404, throws the exception generated by the specified
     * {@link Supplier}. If the response has an other non-successful status (not 2xx), throws a {@link IOException}.
     *
     * @param <E> the type of the exception thrown in case of 404
     * @param downloadUri
     * @throws E           if the HTTP response has status 404
     * @throws IOException if the HTTP response has an other non-success status (not 2xx) or if there was any other error
     */
    private <E extends Throwable> InputStream getInputStream(URI downloadUri, Supplier<E> status404ExceptionSupplier) throws E, IOException {
        log.info("Downloading from URL {}", downloadUri);

        // perform call
        ResponseEntity<Resource> responseEntity = restTemplate.getForEntity(downloadUri, Resource.class);

        // map 404 to exception using specified Supplier
        if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw status404ExceptionSupplier.get();
        }
        // map any other non-successful status to IOException
        assertResponseSuccessful(responseEntity);

        //noinspection ConstantConditions
        return responseEntity.getBody().getInputStream();
    }

    private void assertResponseSuccessful(ResponseEntity<?> responseEntity) throws IOException {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new IOException("Expected status code 2xx, got: " + responseEntity);
        }
        if (responseEntity.getBody() == null) {
            throw new IOException("Response body is null");
        }
    }

}
