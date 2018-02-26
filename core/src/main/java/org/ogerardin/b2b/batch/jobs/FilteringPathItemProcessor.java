package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
class FilteringPathItemProcessor implements ItemProcessor<FileInfo, FileInfo> {

    /** The hash engine to use. See implementations of {@link MD5Calculator} */
    private final MD5Calculator md5Calculator;

    private final StorageService storageService;

    FilteringPathItemProcessor(@NonNull StorageService storageService, @NonNull MD5Calculator md5Calculator) {
        this.storageService = storageService;
        this.md5Calculator = md5Calculator;
    }

    @Override
    public FileInfo process(FileInfo item) throws Exception {

        Path path = item.getPath();

        // retrieve MD5 of stored file version
        String storedMd5hash;
        try {
            FileVersion info = storageService.getLatestFileVersion(path);
            storedMd5hash = info.getMd5hash();

        } catch (StorageFileNotFoundException e) {
            log.debug("NEW FILE: " + path);
            return item;
        }

        // compute file MD5 and compare with stored file MD5
        byte[] bytes = Files.readAllBytes(path);
        String computedMd5Hash = md5Calculator.hexMd5Hash(bytes);
        if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
            // same MD5, file can be skipped
            log.debug("Unchanged: " + path);
            return null; // returning null instructs Batch to skip the item, i.e. it is not passed to the writer
        }

        log.debug("CHANGED: " + path);
        return item;
    }
}
