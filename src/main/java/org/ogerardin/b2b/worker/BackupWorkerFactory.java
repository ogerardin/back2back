package org.ogerardin.b2b.worker;

import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.util.Factory;
import org.ogerardin.b2b.worker.local.LocalBackupWorker;
import org.ogerardin.b2b.worker.peer.PeerBackupWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BackupWorkerFactory extends Factory<BackupWorkerBase> {

    public BackupWorkerFactory() {
        super(Arrays.asList(
                LocalBackupWorker.class,
                PeerBackupWorker.class
        ));
    }

    public BackupWorkerBase newWorker(BackupSource source, BackupTarget target) throws B2BException {
        try {
            return newInstance(source, target);
        } catch (InstantiationException e) {
            throw new B2BException("Failed to create instance of BackupWorker", e);
        }
    }

    @Bean
    public static BackupWorkerFactory getBackupWorkerFactory() {
        return new BackupWorkerFactory();
    }

}
