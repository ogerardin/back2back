package org.ogerardin.b2b.storage;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class StorageProviderTest {

    private static final String FILESET_RSC = "/fileset";

    private final StorageService storageService;

    @Before
    public void setUp() {
        storageService.init();
        storageService.deleteAll();
    }

    @After
    public void tearDown() {
    }

    public StorageProviderTest(StorageService storageService) {
        this.storageService = storageService;
    }

    @Test
    public void loadAll() throws Exception {

        // list all files in resource directory
        URL url = getClass().getResource(FILESET_RSC);
        List<Path> paths0 = Files.list(Paths.get(url.toURI())).collect(Collectors.toList());

        // store each file in storage service
        paths0.forEach(storageService::store);

        // retrieve paths of stored files
        List<Path> paths1 = storageService.loadAll().collect(Collectors.toList());

        // compare each file to the stored version
        for (Path p1 : paths1) {
            for (Path p0 : paths0) {
                if (p0.endsWith(p1)) {
                    InputStream inputStream1 = storageService.getAsInputStream(p1.toString());
                    InputStream inputStream0 = Files.newInputStream(p0, StandardOpenOption.READ);
                    Assert.assertTrue(IOUtils.contentEquals(inputStream0, inputStream1));
                    paths0.remove(p0);
                    break;
                }
                // the listed file does not match any of the stored files
                Assert.fail("No match for " + p1);
            }
        }

        // make sure all the stored files have been matched
        Assert.assertTrue(paths0.isEmpty());

    }

}