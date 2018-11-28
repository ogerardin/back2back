package org.ogerardin.b2b.batch.jobs.support;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.LatestStoredRevision;
import org.ogerardin.b2b.hash.ByteArrayHashCalculator;
import org.ogerardin.b2b.hash.InputStreamHashCalculator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A {@link Predicate} that decides if a local file should be backep up based on its hash, compared to a stored
 * hash provided by a {@link LatestStoredRevisionProvider}
 */
@Slf4j
@Data
public class HashFilteringStrategy implements Predicate<Path> {

    /** Provides a way to query information (specifically MD5) abouth the stored file */
    private final LatestStoredRevisionProvider latestStoredRevisionProvider;

    /** The hash engine to use. See implementations of {@link ByteArrayHashCalculator} */
    private final InputStreamHashCalculator hashCalculator;

    /**
     * @return true if the local file with the specified path must be backed up
     */
    @Override
    public boolean test(Path path) {
        // retrieve hash of stored file version
        Optional<LatestStoredRevision> info = latestStoredRevisionProvider.getLatestStoredRevision(path);
        if (!info.isPresent()) {
            // no stored information yet: the file is a new file
            log.debug("NEW FILE: " + path);
            return true;
        }
        //FIXME use a generic hash instead of MD5
        String storedHash = info.get().getMd5hash();

        // compute current file's hash and compare with stored hash
        try (FileInputStream fileInputStream = new FileInputStream(path.toFile())){
            String computedHash = hashCalculator.hexHash(fileInputStream);
            if (computedHash.equalsIgnoreCase(storedHash)) {
                // same hash, file can be skipped
                log.debug("Unchanged: " + path);
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to compute hash for " + path, e);
            return false;
        }
        log.debug("CHANGED: " + path);
        return true;
    }
}
