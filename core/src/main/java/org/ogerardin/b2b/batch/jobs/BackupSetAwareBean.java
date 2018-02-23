package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/** Superclass for beans that need access to the job's "backupset.id" parameter and the BackupSet repository */
abstract class BackupSetAwareBean {

    @Autowired
    protected BackupSetRepository backupSetRepository;

    // IMPORTANT: bean must be job-scoped for this to work
    @Value("#{jobParameters['backupset.id']}")
    protected String backupSetId;


    protected BackupSet getBackupSet() {
        return backupSetRepository.findOne(backupSetId);
    }
}
