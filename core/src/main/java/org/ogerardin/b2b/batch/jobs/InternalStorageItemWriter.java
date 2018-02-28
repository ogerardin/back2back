package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.item.ItemWriter;

import java.nio.file.Path;
import java.util.List;

/**
 * ItemWriter implementation that stores the file corresponding to the input {@link Path} into the
 * internal storage.
 */
@Slf4j
class InternalStorageItemWriter implements ItemWriter<LocalFileInfo> {

    private final StorageService storageService;
    private final long throttleDelay;

    /**
     * @param throttleDelay for testing only, introduces a delay after each file
     */
    InternalStorageItemWriter(StorageService storageService, long throttleDelay) {
        this.storageService = storageService;
        this.throttleDelay = throttleDelay;
    }

    @Override
    public void write(List<? extends LocalFileInfo> items) throws Exception {
        for (LocalFileInfo item : items) {
            Path path = item.getPath();
            try {
                log.debug("STORING: " + path);
                storageService.store(path);
            } catch (Exception e) {
                log.error("Failed to store file: " + path, e);
            }

            if (throttleDelay != 0) {
                Thread.sleep(throttleDelay);
            }
        }

    }
}
