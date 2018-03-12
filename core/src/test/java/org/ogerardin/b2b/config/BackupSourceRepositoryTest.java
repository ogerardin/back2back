package org.ogerardin.b2b.config;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.test.AbstractMongoTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupSourceRepositoryTest extends AbstractMongoTest {

    private static final String FILESET_RSC = "/fileset";

    @Autowired
    BackupSourceRepository backupSourceRepository;

    @Test
    public void test() throws URISyntaxException, IOException, InterruptedException {
        backupSourceRepository.deleteAll();

        List<BackupSource> sources = new ArrayList<>();

        {
            URL url = getClass().getResource(FILESET_RSC);
            Path dir = Paths.get(url.toURI());
            FilesystemSource fileSource = new FilesystemSource(Collections.singletonList(dir));
            sources.add(fileSource);
        }
//        sources.add(new FilesystemSource("C:\\Users\\oge\\Downloads"));

        sources.forEach(s -> backupSourceRepository.insert(s));

        List<BackupSource> fetchedSources = backupSourceRepository.findAll();
        Assert.assertThat(fetchedSources, Matchers.equalTo(sources));

//        Thread.sleep(100000);
    }

}