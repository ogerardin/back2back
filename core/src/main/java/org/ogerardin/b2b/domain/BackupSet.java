package org.ogerardin.b2b.domain;

import lombok.Data;
import org.ogerardin.b2b.mongo.cascade.CascadeSave;
import org.springframework.batch.core.JobParameter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a backup task from a single source to a single target.
 */
@Document
@Data
public class BackupSet implements JobParametersPopulator {

    @Id
    private String id;

//    private UUID machineInfo;

    @DBRef
    @CascadeSave
    private BackupSource backupSource;

    @DBRef
    @CascadeSave
    private BackupTarget backupTarget;

    private Instant lastBackupCompleteTime;
    private Instant currentBackupStartTime;
    private Instant nextBackupTime;

    // total files/bytes in the source
    private long fileCount;
    private long size;

    // remaining files/bytes to backup during this run
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
