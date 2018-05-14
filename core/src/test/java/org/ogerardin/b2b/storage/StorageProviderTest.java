package org.ogerardin.b2b.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import javax.crypto.KeyGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class StorageProviderTest<S extends StorageService> {

    private static final String FILESET_RSC = "/fileset";

    private S storageService;
    private Key key;

    protected void setStorageService(S storageService) {
        this.storageService = storageService;
    }

    protected void testStoreAndRetrieve() throws Exception {
        storeRetrieveCompare(
                this::storeUnencrypted,
                this::listFiles,
                this::retrieveUnencrypted
        );
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

    protected void storeRetrieveCompare(Storer storer, Lister lister, Retriever retriever) throws URISyntaxException, IOException {
        // list all files in resource directory
        List<Path> paths0 = getSampleFilesPaths();

        // store each file in storage service
        for (Path path : paths0) {
            storer.store(path);
        }

        // retrieve paths of stored files
        List<Path> paths1 = lister.getAllFiles();

        Assert.assertEquals(paths0.size(), paths1.size());

        // compare each file to the stored version
        for (int i = 0; i < paths0.size(); i++) {
            Path p0 = paths0.get(i);
            Path p1 = paths1.get(i);
            log.debug("p0={}", p0);
            log.debug("p1={}", p1);
            assertStoredVersionMatchesFile(retriever, p0, p1);
        }

    }

    private void assertStoredVersionMatchesFile(Retriever retriever, Path p0, Path p1) throws IOException {
        Assert.assertTrue(p0.endsWith(p1));

        InputStream inputStream0 = Files.newInputStream(p0, StandardOpenOption.READ);
        InputStream inputStream1 = retriever.getAsInputStream(p1);
        Assert.assertTrue(IOUtils.contentEquals(inputStream0, inputStream1));
    }

    private List<Path> getSampleFilesPaths() throws IOException, URISyntaxException {
        URL url = StorageProviderTest.class.getResource(FILESET_RSC);
        return Files.list(Paths.get(url.toURI()))
                .sorted()
                .collect(Collectors.toList());
    }

    private void storeEncrypted(Path p) {
        try {
            storageService.store(p, key);
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

    private void storeUnencrypted(Path path) {
        storageService.store(path);
    }

    private InputStream retrieveUnencrypted(Path path) {
        try {
            return storageService.getAsInputStream(path.toString());
        } catch (StorageFileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Path> listFiles() {
        return storageService.getAllFiles(false)
                .map(FileInfo::getPath)
                .sorted()
                .collect(Collectors.toList());
    }
}