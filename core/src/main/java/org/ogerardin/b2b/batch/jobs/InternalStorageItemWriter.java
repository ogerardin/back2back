package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

import java.nio.file.Path;
import java.util.List;

/**
 * ItemWriter implementation that stores the file corresponding to the input {@link Path} into the
 * internal storage.
 */
@Slf4j
class InternalStorageItemWriter implements ItemWriter<LocalFileInfo> {

    //TODO implement encryption (see PeerItemWriter)

    private final StorageService storageService;

    /** repository to store the hash and ozher info of backed up files */
    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    private final long throttleDelay;

    /**
     * @param fileBackupStatusInfoProvider
     * @param throttleDelay for testing only, introduces a delay after each file
     */
    InternalStorageItemWriter(StorageService storageService, FileBackupStatusInfoProvider fileBackupStatusInfoProvider, long throttleDelay) {
        this.storageService = storageService;
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
        this.throttleDelay = throttleDelay;
    }

    @Override
    public void write(@NonNull List<? extends LocalFileInfo> items) throws Exception {
        for (LocalFileInfo item : items) {
            Path path = item.getPath();
            try {
                log.debug("STORING: " + path);
                storageService.store(path);
            } catch (Exception e) {
                log.error("Failed to store file: " + path, e);
            }

            // update stored hash
            log.debug("Updating local hash for {} -> {}", path, item.getHashes());
            val peerRevision = new FileBackupStatusInfo(path.toString(), item.getHashes());
            fileBackupStatusInfoProvider.saveRevisionInfo(peerRevision);

            // delay if required
            if (throttleDelay != 0) {
                log.debug("Throttling for {} ms", throttleDelay);
                Thread.sleep(throttleDelay);
            }
        }

    }
}
