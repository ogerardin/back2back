package org.ogerardin.b2b.batch.jobs.support;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.hash.ByteArrayHashCalculator;
import org.ogerardin.b2b.hash.HashProvider;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A {@link Predicate} that decides if a local file should be backep up based on comparing its computed hash to a stored
 * hash provided by a {@link FileBackupStatusInfoProvider}
 */
@Slf4j
@Data
public class HashFilteringStrategy implements Predicate<LocalFileInfo> {

    /** Provides a way to query information (most importantly file hash) abouth the stored file */
    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    /** The hash engine to use. See implementations of {@link ByteArrayHashCalculator} */
    private final HashProvider hashCalculator;

    /**
     * @return true if the local file with the specified path must be backed up
     */
    @Override
    public boolean test(LocalFileInfo item) {

        Path path = item.getPath();

        // retrieve hash of stored file version
        Optional<FileBackupStatusInfo> info = fileBackupStatusInfoProvider.getLatestStoredRevision(path);
        if (!info.isPresent()) {
            // no stored information: the file is a new file
            log.debug("NEW FILE: {}", path);
            // backup requested
            return true;
        }


        String hashName = hashCalculator.name();
        String storedHash = info.get().getHashes().get(hashName);

        if (storedHash == null) {
            // no stored hash for the current hashing algorithm (might happen if the hash provider has changed since last backup)
            log.debug("NO {} HASH FOR: {}", hashName, path);
            // backup requested (this will store the newly computed hash)
            return true;
        }

        // compare current file's hash with stored hash
        String computedHash = item.getHashes().get(hashName);
        if (storedHash.equalsIgnoreCase(computedHash)) {
            // same hash, file can be skipped
            log.debug("Unchanged: {}", path);
            // mark the file as "not deleted"
            fileBackupStatusInfoProvider.touch(path);
            // backup NOT requested
            return false;
        }

        log.debug("CHANGED: {}", path);
        // backup requested
        return true;
    }
}
