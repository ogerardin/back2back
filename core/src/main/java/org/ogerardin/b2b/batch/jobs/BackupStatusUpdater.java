package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * A {@link ItemWriter} that just saves {@link FileBackupStatusInfo}s into a specified {@link FileBackupStatusInfoProvider}
 */
@Slf4j
class BackupStatusUpdater implements ItemWriter<FileBackupStatusInfo> {

    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    BackupStatusUpdater(FileBackupStatusInfoProvider fileBackupStatusInfoProvider) {
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
    }

    @Override
    public void write(@NonNull List<? extends FileBackupStatusInfo> items) {
        for (FileBackupStatusInfo item : items) {
            fileBackupStatusInfoProvider.saveStatusInfo(item);
        }
    }
}
