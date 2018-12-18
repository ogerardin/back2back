package org.ogerardin.b2b.batch.jobs.support;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A {@link Predicate} that decides if a local file should be backep up based on comparing its computed hash to a stored
 * hash provided by a {@link FileBackupStatusInfoProvider}
 */
@Component
@Slf4j
@Data
public class HashFilteringStrategy implements Predicate<FileBackupStatusInfo> {

    /**
     * @return true if the local file with the specified path must be backed up
     */
    @Override
    public boolean test(FileBackupStatusInfo item) {

        String path = item.getPath();

        // retrieve hash of stored file version
        Map<String, String> currentHashes = item.getCurrentHashes();
        Map<String, String> lastSuccessfulBackupHashes = item.getLastSuccessfulBackupHashes();

        if (lastSuccessfulBackupHashes.isEmpty()) {
            // no stored information: the file is a new file
            log.debug("NEW FILE: {}", path);
            // backup requested
            return true;
        }

        Set<String> commonHashNames = Sets.intersection(
                lastSuccessfulBackupHashes.keySet(),
                currentHashes.keySet()
        );

        if (commonHashNames.isEmpty()) {
            //no common hash type -> must assume possibly changed
            return true;
        }

        for (String hashName : commonHashNames) {
            @NonNull String lastBackupHash = lastSuccessfulBackupHashes.get(hashName);
            @NonNull String currentHash = currentHashes.get(hashName);
            if (!Objects.equals(lastBackupHash, currentHash)) {
                // found a hash type with different values -> file changed
                log.debug("{} differenc: saved {}, current {}: {}", hashName, lastBackupHash, currentHash, path);
                return true;
            }
        }

        // else file considered unchanged
        return false;
    }
}
