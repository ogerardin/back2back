package org.ogerardin.b2b.storage.gridfs;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@DataMongoTest
@SpringBootTest
public class GridFsStorageProviderTest {

    private static final String FILESET_RSC = "/fileset";

    @Qualifier("gridFsStorageProvider")
    @Autowired
    StorageService storageService;

    @Before
    public void setUp() {
        storageService.deleteAll();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void loadAll() throws Exception {

        // list all files in resource directory
        URL url = getClass().getResource(FILESET_RSC);
        File dir = new File(url.toURI());
        List<File> files = Arrays.asList(dir.listFiles());

        // store each file in storage service
        for (File file : files) {
            storageService.store(file);
        }

        // retrieve paths of stored files
        List<Path> paths1 = storageService.loadAll().collect(Collectors.toList());

        // compare each file to the stored version
        for (Path path : paths1) {
            InputStream inputStream1 = storageService.getAsInputStream(path.toFile().getCanonicalPath());
            InputStream inputStream0 = Files.newInputStream(path, StandardOpenOption.READ);
            Assert.assertTrue(IOUtils.contentEquals(inputStream0, inputStream1));
        }



    }

}