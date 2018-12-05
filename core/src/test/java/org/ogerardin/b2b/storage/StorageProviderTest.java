package org.ogerardin.b2b.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.security.Key;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class StorageProviderTest<S extends StorageService> {

    /**
     * Resource folder containing a set of files to use for testing
     */
    private static final String FILESET_RSC = "/fileset";

    private S storageService;
    private Key key;

    protected void setStorageService(S storageService) {
        this.storageService = storageService;
    }

    @Before
    public void setUp() throws Exception {
        storageService.deleteAll();
    }

    @After
    public void tearDown() throws Exception {
        storageService.deleteAll();
    }

    protected void testStoreAndRetrieve() throws Exception {
        storeRetrieveCompare(
                this::storeUnencrypted,
                this::listFiles,
                this::retrieveUnencrypted
        );
    }

    protected void testStoreAndRetrieveById() throws Exception {
        storeRetrieveByRevisionId(
                this::storeUnencrypted,
                this::retrieveRevisionUnencrypted);
    }

    protected void testStoreAndRetrieveEncrypted() throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        key = keygen.generateKey();

        storeRetrieveCompare(
                this::storeEncrypted,
                this::listFiles,
                this::retrieveEncrypted
        );
    }

    protected void testMultipleRevisions() throws Exception {
        storeMultipleRevisions(
                this::storeUnencrypted,
                this::listRevisions,
                this::retrieveRevisionUnencrypted
        );
    }

    private void storeRetrieveByRevisionId(Storer storer, RevisionRetriever retriever) throws Exception {
        // list all files in resource directory
        List<Path> paths0 = getSampleFilesPaths();

        // store each file in storage service
        for (Path path : paths0) {
            log.info("Storing {}", path);
            String revId = storer.store(path);
            log.info("stored as revision {}", revId);

            assertStoredRevisionMatchesFile(retriever, path, revId);
        }

    }
    private void storeRetrieveCompare(Storer storer, FileLister lister, PathRetriever retriever) throws URISyntaxException, IOException {
        // list all files in resource directory
        List<Path> paths0 = getSampleFilesPaths();

        // store each file in storage service
        for (Path path : paths0) {
            log.info("Storing {}", path);
            storer.store(path);
        }

        // retrieve paths of stored files
        List<Path> paths1 = lister.getAllFiles();
        log.info("Retrieved file list: {}", paths1);

        Assert.assertEquals(paths0.size(), paths1.size());

        // compare each file to the stored version
        for (int i = 0; i < paths0.size(); i++) {
            Path p0 = paths0.get(i);
            Path p1 = paths1.get(i);
            assertStoredVersionMatchesFile(retriever, p0, p1);
        }

    }

    private void storeMultipleRevisions(Storer storer, RevisionLister lister, RevisionRetriever retriever) throws IOException, URISyntaxException, StorageFileRevisionNotFoundException, StorageFileNotFoundException {
        // list all files in resource directory
        List<Path> paths0 = getSampleFilesPaths();

        Path tempFile = Files.createTempFile("", ".bin");

        // store each file as a revision of the same file
        for (Path path : paths0) {
//            log.info("Copying {} to {}", path, tempFile);
            log.info("Creating new revision of {}", tempFile);
            Files.copy(path, tempFile, StandardCopyOption.REPLACE_EXISTING);
            storer.store(tempFile);
        }

        List<RevisionInfo> allRevisions = lister.getAllRevisions(tempFile);
        allRevisions.sort(Comparator.comparing(RevisionInfo::getStoredDate));
        log.info("Retrieved revision list: {}", allRevisions);

        Assert.assertEquals(paths0.size(), allRevisions.size());

        // compare each file to the stored revision
        for (int i = 0; i < paths0.size(); i++) {
            Path path = paths0.get(i);
            RevisionInfo revision = allRevisions.get(i);
            String revisionId = revision.getId();
            assertStoredRevisionMatchesFile(retriever, path, revisionId);
        }

    }

    private void assertStoredRevisionMatchesFile(RevisionRetriever retriever, Path path, String revisionId) throws IOException, StorageFileRevisionNotFoundException, StorageFileNotFoundException {
        log.info("Veryfing stored revision {} against local file {}", revisionId, path);
        try (
                InputStream inputStream0 = Files.newInputStream(path, StandardOpenOption.READ);
                InputStream inputStream1 = retriever.getAsInputStream(revisionId);
        ) {
            Assert.assertTrue(IOUtils.contentEquals(inputStream0, inputStream1));
        }
    }

    private void assertStoredVersionMatchesFile(PathRetriever retriever, Path p0, Path p1) throws IOException {

        log.info("Veryfing stored file {} against local file {}", p1, p0);

        Assert.assertTrue(p0.endsWith(p1));

        try (
                InputStream inputStream0 = Files.newInputStream(p0, StandardOpenOption.READ);
                InputStream inputStream1 = retriever.getAsInputStream(p1)
        ) {
            Assert.assertTrue(IOUtils.contentEquals(inputStream0, inputStream1));
        }
    }

    private List<Path> getSampleFilesPaths() throws IOException, URISyntaxException {
        URL url = StorageProviderTest.class.getResource(FILESET_RSC);
        return Files.list(Paths.get(url.toURI()))
                .sorted()
                .collect(Collectors.toList());
    }

    private String storeEncrypted(Path p) {
        try {
            return storageService.store(p, key);
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream retrieveEncrypted(Path path) {
        try {
            return storageService.getAsInputStream(path.toString(), key);
        } catch (StorageFileNotFoundException | EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    private String storeUnencrypted(Path path) {
        return storageService.store(path);
    }

    private InputStream retrieveUnencrypted(Path path) {
        try {
            return storageService.getAsInputStream(path);
        } catch (StorageFileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream retrieveRevisionUnencrypted(String revisionId) throws StorageFileRevisionNotFoundException, IOException, StorageFileNotFoundException {
        return storageService.getRevisionAsInputStream(revisionId);
    }

    private List<Path> listFiles() {
        return storageService.getAllFiles(false)
                .map(FileInfo::getPath)
                .sorted()
                .collect(Collectors.toList());
    }

    private List<RevisionInfo> listRevisions(Path path) {
        return Arrays.asList(storageService.getRevisions(path));
    }
}