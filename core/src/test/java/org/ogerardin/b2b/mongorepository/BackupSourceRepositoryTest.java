package org.ogerardin.b2b.mongorepository;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.ogerardin.b2b.domain.entity.BackupSource;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;

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
    public void test() throws URISyntaxException {
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