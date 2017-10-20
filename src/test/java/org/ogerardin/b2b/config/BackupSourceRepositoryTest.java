package org.ogerardin.b2b.config;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@DataMongoTest
@SpringBootTest
public class BackupSourceRepositoryTest {

    private static final String FILESET_RSC = "/fileset";

    @Autowired
    BackupSourceRepository backupSourceRepository;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void test() throws URISyntaxException, IOException {
        backupSourceRepository.deleteAll();

        List<BackupSource> sources = new ArrayList<>();

        {
            URL url = getClass().getResource(FILESET_RSC);
            File dir = new File(url.toURI());
            BackupSource fileSource = new FilesystemSource(dir);
            sources.add(fileSource);
        }
//        sources.add(new FilesystemSource("C:\\Users\\oge\\Downloads"));

        sources.forEach(s -> backupSourceRepository.insert(s));

        List<BackupSource> fetchedSources = backupSourceRepository.findAll();
        Assert.assertThat(fetchedSources, Matchers.equalTo(sources));
    }

}