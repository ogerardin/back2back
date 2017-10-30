package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.files.JavaMD5Calculator;
import org.ogerardin.b2b.files.MD5Calculator;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StoredFileInfo;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ItemProcessor implementation that performs backup of a {@link Path} item to a StorageService.
 * The file is only stored if it hasn't been stored yet or the locally computed MD5 hash is different from the
 * stored file's MD5 hash.
 */
class LocalStorageItemProcessor implements ItemProcessor<Path, PathItemResult> {

    private static final Log logger = LogFactory.getLog(FilesystemSourceBackupJob.class);

    private static final MD5Calculator md5Calculator = new JavaMD5Calculator();

    private final StorageService storageService;
    private final long throttleDelay;

    LocalStorageItemProcessor(StorageService storageService, long throttleDelay) {
        this.storageService = storageService;
        this.throttleDelay = throttleDelay;
    }

    @Override
    public PathItemResult process(Path itemPath) throws Exception {
        logger.debug("Processing " + itemPath);

        try {
            StoredFileInfo info = storageService.query(itemPath);
            String storedMd5hash = info.getMd5hash();

            if (storedMd5hash != null) {
                byte[] bytes = Files.readAllBytes(itemPath);
                String computedMd5Hash = md5Calculator.hexMd5Hash(bytes);
                if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
                    logger.debug("  Hash unchanged, skipping file");
                    return new PathItemResult(itemPath, BackupResult.UNCHANGED);
                }
            }
        } catch (StorageFileNotFoundException e) {
            // file not stored yet, proceed
        }

        PathItemResult result;
        try {
            storageService.store(itemPath);
            result = new PathItemResult(itemPath, BackupResult.BACKED_UP);
        } catch (Exception e) {
            logger.error("Failed to store file: " + itemPath, e);
            result = new PathItemResult(itemPath, BackupResult.ERROR);
        }
        if (throttleDelay != 0) {
            Thread.sleep(throttleDelay);
        }

        return result;
    }
}
