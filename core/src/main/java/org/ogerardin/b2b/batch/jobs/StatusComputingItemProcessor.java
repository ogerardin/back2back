package org.ogerardin.b2b.batch.jobs;

import lombok.val;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Path;
import java.util.Optional;

public class StatusComputingItemProcessor implements ItemProcessor<LocalFileInfo, FileBackupStatusInfo> {

    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    public StatusComputingItemProcessor(FileBackupStatusInfoProvider fileBackupStatusInfoProvider) {
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
    }

    @Override
    public FileBackupStatusInfo process(LocalFileInfo item) {
        Path path = item.getPath();
        long size = item.getFileAttributes().size();
        Optional<FileBackupStatusInfo> maybeStatusInfo = fileBackupStatusInfoProvider.getLatestStoredRevision(path);
        val statusInfo = maybeStatusInfo.orElseGet(() -> new FileBackupStatusInfo(path, size));
        statusInfo.setDeleted(false);
        statusInfo.setCurrentHashes(item.getHashes());
        fileBackupStatusInfoProvider.saveStatusInfo(statusInfo);

        return statusInfo;
    }

}
