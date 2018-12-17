package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemProcessor;

import java.util.function.Predicate;

/**
 * An {@link ItemProcessor} that sets the "backup requested" flag on the input {@link FileBackupStatusInfo}
 * according to the result of applying the specified {@link Predicate}
 */
public class BackupFlagComputingItemProcessor implements ItemProcessor<FileBackupStatusInfo, FileBackupStatusInfo> {

    private final Predicate<FileBackupStatusInfo> filter;

    public BackupFlagComputingItemProcessor(Predicate<FileBackupStatusInfo> filter) {
        this.filter = filter;
    }


    @Override
    public FileBackupStatusInfo process(FileBackupStatusInfo item) {
        boolean backupRequested = filter.test(item);
        item.setBackupRequested(backupRequested);
        return item;
    }
}
