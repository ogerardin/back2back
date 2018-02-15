package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.ogerardin.b2b.storage.FileVersion;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ItemProcessor implementation that filters the input {@link Path} item if it has been stored already and the
 * stored version has the same MD5 hash as the locally computed version.
 */
class FilteringPathItemProcessor implements ItemProcessor<Path, Path> {

    private static final Log logger = LogFactory.getLog(FilteringPathItemProcessor.class);

    private final MD5Calculator md5Calculator; // = new JavaMD5Calculator();

    private final StorageService storageService;

    FilteringPathItemProcessor(StorageService storageService, MD5Calculator md5Calculator) {
        this.storageService = storageService;
        this.md5Calculator = md5Calculator;
    }

    @Override
    public Path process(Path item) throws Exception {
        logger.debug("Processing " + item);

        try {
            FileVersion info = storageService.getLatestFileVersion(item);
            String storedMd5hash = info.getMd5hash();

            if (storedMd5hash != null) {
                // we have a stored version of the file with MD5 information
                // compute file MD5 and compare with stored file MD5
                byte[] bytes = Files.readAllBytes(item);
                String computedMd5Hash = md5Calculator.hexMd5Hash(bytes);
                if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
                    // same MD5, file can be skipped
                    logger.debug("(File hash unchanged, skip)");
                    return null; // returning null instructs Batch to skip the item, i.e. it is not passed to the writer
                }
            }
        } catch (StorageFileNotFoundException e) {
            // file not stored yet
        }

        // item will be passed to ItemWriter for backing up
        return item;
    }
}
