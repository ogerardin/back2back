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

    /** The hash engine to use. See implementations of {@link MD5Calculator} */
    private final MD5Calculator md5Calculator;

    private final StorageService storageService;

    FilteringPathItemProcessor(StorageService storageService, MD5Calculator md5Calculator) {
        this.storageService = storageService;
        this.md5Calculator = md5Calculator;
    }

    @Override
    public Path process(Path item) throws Exception {

        String storedMd5hash;
        try {
            // retrieve MD5 of stored file version
            FileVersion info = storageService.getLatestFileVersion(item);
            storedMd5hash = info.getMd5hash();

        } catch (StorageFileNotFoundException e) {
            logger.debug("INITIAL BACK UP: " + item);
            return item;
        }

        // compute file MD5 and compare with stored file MD5
        byte[] bytes = Files.readAllBytes(item);
        String computedMd5Hash = md5Calculator.hexMd5Hash(bytes);
        if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
            // same MD5, file can be skipped
            logger.debug("Unchanged: " + item);
            return null; // returning null instructs Batch to skip the item, i.e. it is not passed to the writer
        }

        logger.debug("CHANGED: " + item);
        return item;
    }
}
