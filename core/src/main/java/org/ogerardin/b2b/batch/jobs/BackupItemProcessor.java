package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;

/**
 * {@link ItemProcessor} implementation that stores the file corresponding to the input {@link FileBackupStatusInfo}
 * into a {@link StorageService}
 */
@Slf4j
class BackupItemProcessor implements ItemProcessor<FileBackupStatusInfo, FileBackupStatusInfo> {

    //TODO implement encryption (see PeerItemWriter)

    private final StorageService storageService;

    private final long throttleDelay;

    /**
     * @param throttleDelay for testing only, introduces a delay after each file
     */
    BackupItemProcessor(StorageService storageService, long throttleDelay) {
        this.storageService = storageService;
        this.throttleDelay = throttleDelay;
    }

    @Override
    public FileBackupStatusInfo process(@NonNull FileBackupStatusInfo item) throws Exception {
        Instant now = Instant.now();
        item.setLastBackupAttempt(now);

        Path path = Paths.get(item.getPath());
        try {
            if (item.isDeleted()) {
                log.debug("MARKING AS DELETED: " + path);
                storageService.delete(path);
                item.setLastSuccessfulBackupHashes(Collections.emptyMap());
                //entry will be deleted during cleanup step
            }
            else if (item.isBackupRequested()) {
                log.debug("STORING: " + path);
                storageService.store(path);
                item.setLastSuccessfulBackupHashes(item.getCurrentHashes());
                item.setBackupRequested(false);
            }
            item.setLastSuccessfulBackup(now);
            item.setLastBackupAttemptError(null);
        } catch (Exception e) {
            log.error("Failed to store file: " + path, e);
            item.setLastBackupAttemptError(e.toString());
        }

        // delay if required
        if (throttleDelay != 0) {
            log.debug("Throttling for {} ms", throttleDelay);
            Thread.sleep(throttleDelay);
        }

        return item;
    }
}
