package org.ogerardin.b2b.batch.jobs.listeners;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.util.FormattingHelper;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Component
@JobScope
@Slf4j
public class FileHashListener extends BackupSetAwareBean implements ItemWriteListener<LocalFileInfo> {

    @Autowired
    BackupSetStatusPublisher backupSetStatusPublisher;

    @Override
    public void beforeWrite(List<? extends LocalFileInfo> items) {
    }

    /**
     * Update the total file count and size in the BackupSet, amd publish it.
     * This method is called for every chunk of items (as set in the Step definition), so the chink size should
     * not be too low
     */
    @Override
    public void afterWrite(List<? extends LocalFileInfo> items) {
        BackupSet backupSet = getBackupSet();
        long chunkSize = items.stream()
                .map(LocalFileInfo::getFileAttributes)
                .mapToLong(BasicFileAttributes::size)
                .sum();

        long fileCount = backupSet.getFileCount() + items.size();
        long byteCount = backupSet.getSize() + chunkSize;

        String status = "Found: " + fileCount + " file(s), " + FormattingHelper.humanReadableByteCount(byteCount);
        log.info(status);

        backupSet.setFileCount(fileCount);
        backupSet.setSize(byteCount);
        backupSet.setStatus(status);

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

    @Override
    public void onWriteError(Exception exception, List<? extends LocalFileInfo> items) {
    }

}
