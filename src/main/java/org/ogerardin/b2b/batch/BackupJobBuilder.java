package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.springframework.batch.core.Job;

public interface BackupJobBuilder {

    Job newBackupJob(BackupSource source, BackupTarget target) throws B2BException;
}
