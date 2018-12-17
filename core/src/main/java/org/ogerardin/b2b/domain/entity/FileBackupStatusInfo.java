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

    private long size;

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

    /**
     * {@code true} if the corresponding file must be backed up in the next batch
     */
    private boolean backupRequested = true;

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
    public FileBackupStatusInfo(String path, String md5hash, boolean deleted) {
        this.path = path;
        this.deleted = deleted;
        this.lastSuccessfulBackupHashes.put("MD5", md5hash);
    }

    public FileBackupStatusInfo(Path path, long size) {
        this(path.toString(), size);
    }

    public FileBackupStatusInfo(String path, long size) {
        this.path = path;
        this.size = size;
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
