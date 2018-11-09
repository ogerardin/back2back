package org.ogerardin.b2b.batch.jobs.listeners;

import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/** Superclass for beans that need access to the job's "backupset.id" parameter and the BackupSet repository */
public abstract class BackupSetAwareBean {

    @Autowired
    protected BackupSetRepository backupSetRepository;

    // IMPORTANT: bean must be job-scoped for this to work
    @Value("#{jobParameters['backupset.id']}")
    protected String backupSetId;


    protected BackupSet getBackupSet() {
        return backupSetRepository.findById(backupSetId).get();
    }
}
