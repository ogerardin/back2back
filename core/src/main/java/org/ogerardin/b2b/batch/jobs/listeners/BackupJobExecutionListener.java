package org.ogerardin.b2b.batch.jobs.listeners;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Execution listener that updates corresponding {@link BackupSet}
 */
@Component
@JobScope
@Slf4j
public class BackupJobExecutionListener extends BackupSetAwareBean implements JobExecutionListener {

    @Autowired
    B2BProperties properties;

    @Autowired
    BackupSetStatusPublisher backupSetStatusPublisher;

    @Override
    public void beforeJob(JobExecution jobExecution) {
//        logger.debug("beforeJob, jobExecution=" + jobExecution);
        BackupSet backupSet = getBackupSet();
        backupSet.setCurrentBackupStartTime(jobExecution.getStartTime().toInstant());

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
//        logger.debug("afterJob, jobExecution=" + jobExecution);
        Instant completeTime = jobExecution.getEndTime().toInstant();

        BackupSet backupSet = getBackupSet();
        backupSet.setLastBackupCompleteTime(completeTime);
//        backupSet.setBatchCount(0);
//        backupSet.setBatchSize(0);
        backupSet.getBatchFiles().reset();

        String status = "Complete";
        long pauseAfterBackup = properties.getPauseAfterBackup();
        Instant nextBackupTime = completeTime.plusMillis(pauseAfterBackup);
        backupSet.setNextBackupTime(nextBackupTime);
        status += " (next backup at " + nextBackupTime + ")";
        backupSet.setStatus(status);

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

}
