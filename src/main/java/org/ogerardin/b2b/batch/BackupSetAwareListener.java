package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class BackupSetAwareListener {

    @Autowired
    protected BackupSetRepository backupSetRepository;

    @Value("#{jobParameters['backupset.id']}")
    protected String backupSetId;


    protected BackupSet getBackupSet() {
        return backupSetRepository.findOne(backupSetId);
    }
}
