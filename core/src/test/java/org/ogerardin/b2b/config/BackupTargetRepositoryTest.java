package org.ogerardin.b2b.config;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.domain.PeerTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.ogerardin.b2b.test.AbstractMongoTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BackupTargetRepositoryTest extends AbstractMongoTest {

    @Autowired
    BackupTargetRepository backupTargetRepository;

    @Test
    public void test() throws IOException {

        LocalTarget target0 = new LocalTarget();

        PeerTarget target1 = new PeerTarget("127.0.0.1", 80);
        target1.setEnabled(false);

        List<BackupTarget> targets = Arrays.asList(target0, target1);

        backupTargetRepository.deleteAll();

        targets.forEach(t -> backupTargetRepository.insert(t));

        List<BackupTarget> fetchedTargets = backupTargetRepository.findAll();
        Assert.assertThat(fetchedTargets, Matchers.equalTo(targets));

    }

}