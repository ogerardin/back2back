package org.ogerardin.b2b.domain.entity;

import com.google.common.collect.Sets;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Meta-data about a backed up file.
 */
@Document
@Data
public class FileBackupStatusInfo {

    @Id
    private String path;

    @Deprecated
    private String md5hash;

    /**
     * Hashes of the last backed up revision of the file
     */
    private Map<String, String> lastSuccessfulBackupHashes = new HashMap<>();

    /**
     * Hashes of the current file on disk (only if different from {@link #lastSuccessfulBackupHashes}
     */
    private Map<String, String> currentHashes = new HashMap<>();

    /**
     * {@code true} if the file has been deleted from the filesystem since last backup
     */
    private boolean deleted = false;

    private Instant lastSuccessfulBackup;

    private Instant lastBackupAttempt;

    private String lastBackupAttemptError;

    public FileBackupStatusInfo() {
    }

    public FileBackupStatusInfo(String path) {
        this.path = path;
    }

    public FileBackupStatusInfo(Path path) {
        this(path.toString());
    }

    @Deprecated
    public FileBackupStatusInfo(String path, Map<String, String> lastSuccessfulBackupHashes) {
        this.path = path;
        this.lastSuccessfulBackupHashes = lastSuccessfulBackupHashes;
    }

    @Deprecated
    public FileBackupStatusInfo(String path, String md5hash, boolean deleted) {
        this.path = path;
        this.md5hash = md5hash;
        this.deleted = deleted;
        this.lastSuccessfulBackupHashes.put("MD5", md5hash);
    }

    /**
     * @return true if the file has changed since last backup, i.e. at least one of the common hash types between
     *  {@link #lastSuccessfulBackupHashes} and {@link #currentHashes} has different values, or there are no
     *  common hash types.
     */
    public boolean fileChanged() {
        Set<String> commonHashNames = Sets.intersection(
                lastSuccessfulBackupHashes.keySet(),
                currentHashes.keySet()
        );

        if (commonHashNames.isEmpty()) {
            // no common hash type -> must assume possibly changed
            return true;
        }

        for (String hashName : commonHashNames) {
            @NonNull String lastBackupHash = lastSuccessfulBackupHashes.get(hashName);
            @NonNull String currentHash = currentHashes.get(hashName);
            if (!Objects.equals(lastBackupHash, currentHash)) {
                // found a hash type with different values -> file changed
                return true;
            }
        }

        // else file considered unchanged
        return false;
    }
}
