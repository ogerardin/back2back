package org.ogerardin.b2b.config;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@DataMongoTest
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BackupSourceRepositoryTest {

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

    @Test
    public void init() throws URISyntaxException, IOException, InterruptedException {
        backupSourceRepository.deleteAll();

        {
            Path dir = Paths.get("C:\\Users\\oge\\Downloads");
            FilesystemSource fileSource = new FilesystemSource(Collections.singletonList(dir));
            backupSourceRepository.insert(fileSource);
        }
        Thread.sleep(100000);
    }

}