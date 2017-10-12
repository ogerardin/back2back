package org.ogerardin.b2b.config;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.domain.*;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@DataMongoTest
@SpringBootTest
public class BackupTargetRepositoryTest {

    @Autowired
    BackupTargetRepository backupTargetRepository;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    @Test
    public void test() throws IOException {
        LocalTarget target0 = new LocalTarget(new File("backup-target").getCanonicalPath());
        NetworkTarget target1 = new NetworkTarget("127.0.0.1", 80);
        List<BackupTarget> targets = Arrays.asList(target0, target1);

        backupTargetRepository.deleteAll();

        backupTargetRepository.insert(target0);
        backupTargetRepository.insert(target1);

        List<BackupTarget> fetchedTargets = backupTargetRepository.findAll();

        Assert.assertThat(fetchedTargets, Matchers.equalTo(targets));

    }

}