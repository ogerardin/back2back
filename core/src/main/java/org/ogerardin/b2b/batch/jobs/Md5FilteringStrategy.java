package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.ogerardin.b2b.files.md5.StreamingMd5Calculator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A filter that selects a {@link Path} for backup based on its MD5 hash
 */
@Slf4j
@Data
public class Md5FilteringStrategy implements Predicate<Path> {

    /**Provides a way to query information (specifically MD5) abouth the stored file */
    private final StoredFileVersionInfoProvider storedFileVersionInfoProvider;

    /** The hash engine to use. See implementations of {@link MD5Calculator} */
    private final StreamingMd5Calculator md5Calculator;

    /**
     * @return true if the local file with the specified path must be backed up
     */
    @Override
    public boolean test(Path path) {
        // retrieve MD5 of stored file version
        Optional<StoredFileVersionInfo> info = storedFileVersionInfoProvider.getStoredFileVersionInfo(path);
        if (!info.isPresent()) {
            // no stored information yet: the file is a new file
            log.debug("NEW FILE: " + path);
            return true;
        }
        String storedMd5hash = info.get().getMd5hash();

        // compute current file's MD5 and compare with stored file MD5
        try {
            FileInputStream fileInputStream = new FileInputStream(path.toFile());
            String computedMd5Hash = md5Calculator.hexMd5Hash(fileInputStream);
            if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
                // same MD5, file can be skipped
                log.debug("Unchanged: " + path);
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to compute MD5 for " + path, e);
            return false;
        }
        log.debug("CHANGED: " + path);
        return true;
    }
}
