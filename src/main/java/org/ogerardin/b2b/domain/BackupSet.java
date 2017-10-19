package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.batch.core.JobParameter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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

    private String lastFile;
    private String currentFile;

    private String lastError;

    public void populateParams(Map<String, JobParameter> params) {
        params.put("backupset.id", new JobParameter(getId(), false));
    }

}
