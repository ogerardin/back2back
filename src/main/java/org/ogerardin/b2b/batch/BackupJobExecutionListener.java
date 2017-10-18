package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

/**
 * Execution listener that updates corresponding {@link BackupSet}
 */
@Component
@JobScope
public class BackupJobExecutionListener extends BackupSetAwareListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setCurrentBackupStartTime(jobExecution.getStartTime().toInstant());
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setLastBackupCompleteTime(jobExecution.getEndTime().toInstant());
        backupSetRepository.save(backupSet);
    }

}
