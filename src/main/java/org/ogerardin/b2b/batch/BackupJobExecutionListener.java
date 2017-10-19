package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Execution listener that updates corresponding {@link BackupSet}
 */
@Component
@JobScope
public class BackupJobExecutionListener extends BackupSetAwareListener implements JobExecutionListener {

    private static final Log logger = LogFactory.getLog(BackupJobExecutionListener.class);

    @Autowired
    private JobOperator jobOperator;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.debug("beforeJob, jobExecution=" + jobExecution);
        BackupSet backupSet = getBackupSet();
        backupSet.setCurrentBackupStartTime(jobExecution.getStartTime().toInstant());
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.debug("afterJob, jobExecution=" + jobExecution);
        BackupSet backupSet = getBackupSet();
        backupSet.setLastBackupCompleteTime(jobExecution.getEndTime().toInstant());
        backupSetRepository.save(backupSet);

/*
        try {
            Long jobExecutionId = jobExecution.getId();
            logger.info("Restarting jobId " + jobExecutionId);
            jobOperator.restart(jobExecutionId);
        } catch (JobExecutionException e) {
            logger.error("Failed to restart execution", e);
        }
*/

    }

}
