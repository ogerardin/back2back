package org.ogerardin.b2b.domain;

import lombok.Data;
import org.ogerardin.b2b.batch.jobs.BackupResult;
import org.springframework.batch.core.JobParameter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a backup job from a single source to a single target.
 */
@Document
@Data
public class BackupSet implements JobParametersPopulator {

    @Id
    private String id;

    private UUID computerId;

    @DBRef
    private BackupSource backupSource;

    @DBRef
    private BackupTarget backupTarget;

    private Instant lastBackupCompleteTime;
    private Instant currentBackupStartTime;

    private long fileCount;

    private Path lastFile;
    private BackupResult lastFileStatus;
    private Path currentFile;

    private String lastError;

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("backupset.id", new JobParameter(getId(), false));
    }

}
