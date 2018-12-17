package org.ogerardin.b2b.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.ogerardin.b2b.batch.jobs.support.FileSetStats;
import org.ogerardin.b2b.domain.JobParametersPopulator;
import org.ogerardin.b2b.mongo.cascade.CascadeSave;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a backup stream from a single source to a single target.
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
    FileSetStats allFiles = new FileSetStats();

    // total files/bytes to backup during the current job run
    FileSetStats batchFiles = new FileSetStats();

    // remaining files/bytes to backup during the current job run
    FileSetStats toDoFiles = new FileSetStats();

    private String lastError;

    private String status;

    // Job information
    private String jobName;

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("backupset.id", new JobParameter(getId(), false));
        backupSource.populateParams(params);
        backupTarget.populateParams(params);
    }

    @JsonIgnore
    public JobParameters getJobParameters() {
        Map<String, JobParameter> params = new HashMap<>();
        populateParams(params);
        return new JobParameters(params);
    }

    public void resetState() {
        currentBackupStartTime = null;
        nextBackupTime = null;
        allFiles.reset();
        batchFiles.reset();
        toDoFiles.reset();
        lastError = null;
        status = "Inactive";
        jobName = null;
    }
}
