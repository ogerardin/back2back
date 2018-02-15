package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.BackupSetAwareBean;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Component
@JobScope
public class PathItemWriteListener extends BackupSetAwareBean implements ItemWriteListener<Path> {

    @Override
    public void beforeWrite(List<? extends Path> items) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Backing up " + Arrays.toString(items.toArray()));
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterWrite(List<? extends Path> items) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Finished backing up " + Arrays.toString(items.toArray()));
        backupSetRepository.save(backupSet);
    }

    @Override
    public void onWriteError(Exception exception, List<? extends Path> items) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("ERROR backing up " + Arrays.toString(items.toArray()));
        backupSetRepository.save(backupSet);

    }
}
