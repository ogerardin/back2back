package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * An {@link ItemWriter} that marks the file designated by the specified {@link LocalFileInfo} as existing
 * and stores its current hashes.
 */
public class BackupStatusToucher implements ItemWriter<LocalFileInfo> {

    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    public BackupStatusToucher(FileBackupStatusInfoProvider fileBackupStatusInfoProvider) {
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
    }

    @Override
    public void write(@NonNull List<? extends LocalFileInfo> items) {
        for (LocalFileInfo item : items) {
            fileBackupStatusInfoProvider.touch(item.getPath(), item.getHashes());
        }
    }

}
