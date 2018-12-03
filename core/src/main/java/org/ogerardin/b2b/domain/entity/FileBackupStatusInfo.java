package org.ogerardin.b2b.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta-data about a backed up file.
 */
@Document
@Data
public class FileBackupStatusInfo {

    @Id
    private Path path;

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

    public FileBackupStatusInfo(Path path) {
        this.path = path;
    }

    public FileBackupStatusInfo(Path path, Map<String, String> lastSuccessfulBackupHashes) {
        this.path = path;
        this.lastSuccessfulBackupHashes = lastSuccessfulBackupHashes;
    }

    @Deprecated
    public FileBackupStatusInfo(Path path, String md5hash, boolean deleted) {
        this.path = path;
        this.md5hash = md5hash;
        this.deleted = deleted;
        this.lastSuccessfulBackupHashes.put("MD5", md5hash);
    }

    public FileBackupStatusInfo setHash(String hashName, String hashValue) {
        lastSuccessfulBackupHashes.put(hashName, hashValue);
        return this;
    }
}
