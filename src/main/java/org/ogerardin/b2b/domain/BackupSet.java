package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.batch.core.JobParameter;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class BackupSet implements JobParametersPopulator {

    @Id
    private String id;

    private UUID computerId;

    private String backupSourceId;
    private String backupTargetId;

    private Instant lastBackupCompleteTime;
    private Instant currentBackupStartTime;

    public void populateParams(Map<String, JobParameter> params) {
        params.put("backupset.id", new JobParameter(getId(), false));
    }
}
