package org.ogerardin.b2b.storage;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public abstract class StorageProviderTest<S extends StorageService> {

    protected S storageService;

    private static final String FILESET_RSC = "/fileset";

    protected void setStorageService(S storageService) {
        this.storageService = storageService;
    }

    protected void testLoadAll(StorageService storageService) throws Exception {

        // list all files in resource directory
        URL url = getClass().getResource(FILESET_RSC);
        List<Path> paths0 = Files.list(Paths.get(url.toURI()))
                .sorted()
                .collect(Collectors.toList());

        // store each file in storage service
        paths0.forEach(storageService::store);

        // retrieve paths of stored files
        List<Path> paths1 = storageService.getAllFiles(true)
                .map(FileInfo::getPath)
                .sorted()
                .collect(Collectors.toList());

        Assert.assertEquals(paths0.size(), paths1.size());

        // compare each file to the stored version
        for (int i = 0; i < paths0.size(); i++) {
            Path p0 = paths0.get(i);
            Path p1 = paths1.get(i);
            Assert.assertTrue(p0.endsWith(p1));
            InputStream inputStream1 = storageService.getAsInputStream(p1.toString());
            InputStream inputStream0 = Files.newInputStream(p0, StandardOpenOption.READ);
            Assert.assertTrue(IOUtils.contentEquals(inputStream0, inputStream1));
        }
    }

}