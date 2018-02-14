package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.item.ItemWriter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * ItemWriter implementation that stores the file corresponding to the input {@link Path} into the
 * internal storage.
 */
class InternalStorageItemWriter implements ItemWriter<Path> {

    private static final Log logger = LogFactory.getLog(InternalStorageItemWriter.class);

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
    public void write(List<? extends Path> items) throws Exception {
        logger.debug("Writing " + Arrays.toString(items.toArray()));

        for (Path path : items) {
            try {
                logger.debug("STORING: " + path);
                storageService.store(path);
            } catch (Exception e) {
                logger.error("Failed to store file: " + path, e);
            }

            if (throttleDelay != 0) {
                Thread.sleep(throttleDelay);
            }
        }

    }
}
