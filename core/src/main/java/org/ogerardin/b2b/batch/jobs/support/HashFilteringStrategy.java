package org.ogerardin.b2b.batch.jobs.support;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;

import java.util.function.Predicate;

/**
 * A {@link Predicate} that decides if a local file should be backep up based on comparing its computed hash to a stored
 * hash provided by a {@link FileBackupStatusInfoProvider}
 */
@Slf4j
@Data
public class HashFilteringStrategy implements Predicate<FileBackupStatusInfo> {

    /** The hash algorythm to be used as reference */
    private final String referenceHashName;

    /**
     * @return true if the local file with the specified path must be backed up
     */
    @Override
    public boolean test(FileBackupStatusInfo item) {

        String path = item.getPath();

        // retrieve hash of stored file version
        if (item.getCurrentHashes().isEmpty()) {
            // no stored information: the file is a new file
            log.debug("NEW FILE: {}", path);
            // backup requested
            return true;
        }

        String storedHash = item.getLastSuccessfulBackupHashes().get(referenceHashName);

        if (storedHash == null) {
            // no stored hash for the current hashing algorithm (might happen if the hash provider has changed since last backup)
            log.debug("NO {} HASH FOR: {}", referenceHashName, path);
            // backup requested (this will store the newly computed hash)
            return true;
        }

        // compare current file's hash with stored hash
        String computedHash = item.getCurrentHashes().get(referenceHashName);
        if (storedHash.equalsIgnoreCase(computedHash)) {
            // same hash, no need to backup
            log.debug("Unchanged: {}", path);
            // backup NOT requested
            return false;
        }

        log.debug("CHANGED: {}", path);
        // backup requested
        return true;
    }
}
