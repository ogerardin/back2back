package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.batch.core.JobParameter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private Instant nextBackupTime;

    private long fileCount;
    private long size;

    private long toDoCount;
    private long toDoSize;

//    private Path lastFile;
//    private Path currentFile;

    private String lastError;

    private String status;

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("backupset.id", new JobParameter(getId(), false));

        backupSource.populateParams(params);
        backupTarget.populateParams(params);

    }

}
