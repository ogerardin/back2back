package org.ogerardin.b2b.batch.jobs.listeners;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.FileSetStats;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.util.FormattingHelper;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@JobScope
@Slf4j
public class ComputeBatchStepItemWriteListener extends BackupSetAwareBean implements ItemWriteListener<FileBackupStatusInfo> {

    @Autowired
    BackupSetStatusPublisher backupSetStatusPublisher;

    @Override
    public void beforeWrite(List<? extends FileBackupStatusInfo> items) {
        // nop
    }

    @Override
    public void afterWrite(List<? extends FileBackupStatusInfo> items) {
        BackupSet backupSet = getBackupSet();

        // update backup set stats
        FileSetStats allFilesStats = backupSet.getAllFiles();
        FileSetStats batchFileStats = backupSet.getBatchFiles();
        FileSetStats toDoFileStats = backupSet.getToDoFiles();
        for (FileBackupStatusInfo item : items) {
            long fileSize = item.getSize();
            allFilesStats.addFile(fileSize);
            if (item.isBackupRequested()) {
                batchFileStats.addFile(fileSize);
                toDoFileStats.addFile(fileSize);
            }
        }

        String status = String.format(
                "Found so far: %d file(s) (%s), to backup: %d file(s) (%s)",
                allFilesStats.getFileCount(), FormattingHelper.humanReadableByteCount(allFilesStats.getByteCount()),
                batchFileStats.getFileCount(), FormattingHelper.humanReadableByteCount(batchFileStats.getByteCount())
        );
        log.info(status);

        backupSet.setStatus(status);

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);

    }

    @Override
    public void onWriteError(Exception exception, List<? extends FileBackupStatusInfo> items) {

    }
}
