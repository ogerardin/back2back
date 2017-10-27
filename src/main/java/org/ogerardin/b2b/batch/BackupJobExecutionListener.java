package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.JobLauncher;
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
    B2BProperties properties;

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobLocator jobLocator;

    @Override
    public void beforeJob(JobExecution jobExecution) {
//        logger.debug("beforeJob, jobExecution=" + jobExecution);
        BackupSet backupSet = getBackupSet();
        backupSet.setCurrentBackupStartTime(jobExecution.getStartTime().toInstant());
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
//        logger.debug("afterJob, jobExecution=" + jobExecution);
        BackupSet backupSet = getBackupSet();
        backupSet.setLastBackupCompleteTime(jobExecution.getEndTime().toInstant());
        backupSetRepository.save(backupSet);

        if (properties != null && properties.isAutorestart()) {
            try {
                logger.info("Pausing between job executions");
                Thread.sleep(10000);
                // restart job
                String jobName = jobExecution.getJobInstance().getJobName();
                Job job = jobLocator.getJob(jobName);

                JobParameters jobParameters = jobExecution.getJobParameters();
                jobParameters = job.getJobParametersIncrementer().getNext(jobParameters);
                logger.info("Attempting to restart job " + jobName + " with parameters: " + jobParameters);
                jobLauncher.run(job, jobParameters);
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                logger.error("Failed to restart job", e);
            }
        }
    }

}
