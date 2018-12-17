package org.ogerardin.b2b.batch.jobs.listeners;

import org.ogerardin.b2b.batch.jobs.support.FileSetStats;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@JobScope
public class FileBackupListener extends BackupSetAwareBean implements ItemWriteListener<FileBackupStatusInfo> {

    @Autowired
    BackupSetStatusPublisher backupSetStatusPublisher;

    @Override
    public void beforeWrite(List<? extends FileBackupStatusInfo> items) {
        String[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Backing up " + Arrays.toString(paths));

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

    @Override
    public void afterWrite(List<? extends FileBackupStatusInfo> items) {
        String[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();

        backupSet.setStatus("Finished backing up " + Arrays.toString(paths));
        // subtract written size and count from to do
        subtractFromToDo(items, backupSet);

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

    @Override
    public void onWriteError(Exception exception, List<? extends FileBackupStatusInfo> items) {
        String[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("ERROR backing up " + Arrays.toString(paths));
        // subtract written size and count from to do
        subtractFromToDo(items, backupSet);

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

    private void subtractFromToDo(List<? extends FileBackupStatusInfo> items, BackupSet backupSet) {
        FileSetStats toDoFileStats = backupSet.getToDoFiles();
        items.stream()
                .mapToLong(FileBackupStatusInfo::getSize)
                .forEach(toDoFileStats::subtractFile);
    }

    private String[] getPaths(List<? extends FileBackupStatusInfo> items) {
        return items.stream()
                .map(FileBackupStatusInfo::getPath)
                .toArray(String[]::new);
    }
}
