package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * ItemProcessor implementation that filters (eliminates from further processing) the input {@link Path} item if
 * and only if it has been stored already and the stored version has the same MD5 hash as the locally computed version.
 */
@Data
@Slf4j
class FilteringPathItemProcessor implements ItemProcessor<LocalFileInfo, LocalFileInfo> {

    /** Provides a way to query information (including MD5) abouth the stored file */
    private final StoredFileVersionInfoProvider storedFileVersionInfoProvider;

    /** The hash engine to use. See implementations of {@link MD5Calculator} */
    private final MD5Calculator md5Calculator;


    @Override
    public LocalFileInfo process(LocalFileInfo item) throws Exception {

        Path path = item.getPath();

        // retrieve MD5 of stored file version
        Optional<StoredFileVersionInfo> info = storedFileVersionInfoProvider.getStoredFileVersionInfo(path);
        if (! info.isPresent()) {
            log.debug("NEW FILE: " + path);
            return item;

        }
        String storedMd5hash = info.get().getMd5hash();

        // compute current file's MD5 and compare with stored file MD5
        byte[] bytes = Files.readAllBytes(path);
        String computedMd5Hash = md5Calculator.hexMd5Hash(bytes);
        //TODO maybe it would be more efficient to work with raw bytes instead of doing a String comparison?
        if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
            // same MD5, file can be skipped
            log.debug("Unchanged: " + path);
            return null; // returning null instructs Batch to skip the item, i.e. it is not passed to the writer
        }

        log.debug("CHANGED: " + path);
        return item;
    }
}
