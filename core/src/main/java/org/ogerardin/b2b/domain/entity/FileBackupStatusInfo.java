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

    private FileBackupStatusInfo(Path path, long size) {
        this(path.toString(), size);
    }

    private FileBackupStatusInfo(String path, long size) {
        this.path = path;
        this.size = size;
    }

    public static FileBackupStatusInfo forNewFile(Path path, long size) {
        FileBackupStatusInfo statusInfo = new FileBackupStatusInfo(path, size);
        statusInfo.setBackupRequested(true);
        statusInfo.setDeleted(false);
        return statusInfo;
    }
}
