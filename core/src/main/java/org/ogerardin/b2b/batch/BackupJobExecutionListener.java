package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * Execution listener that updates corresponding {@link BackupSet}
 */
@Component
@JobScope
public class BackupJobExecutionListener extends BackupSetAwareBean implements JobExecutionListener {

    private static final Log logger = LogFactory.getLog(BackupJobExecutionListener.class);

    @Autowired
    B2BProperties properties;

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private JobLocator jobLocator;

    @Autowired
    private AsyncTaskExecutor asyncTaskExecutor;

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

        if (properties != null && properties.isContinuousBackup()) {
            // Pause and schedule a job restart. This is done asynchronously because
            // the current job is not considered as complete until we exit this function.
            scheduleDelayedRestart(jobExecution, properties.getPauseAfterBackup());
        }
    }

    private void scheduleDelayedRestart(JobExecution jobExecution, long delay) {
        asyncTaskExecutor.submit(() -> {
            String jobName = jobExecution.getJobInstance().getJobName();
            JobParameters jobParameters = jobExecution.getJobParameters();
            // pause
            try {
                logger.info("Pausing for " + msToHumanDuration(delay));
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.warn("Restart task interrupted during pause: " + e.toString());
            }
            // restart job
            try {
                Job job = jobLocator.getJob(jobName);
                JobParameters nextJobParameters = job.getJobParametersIncrementer().getNext(jobParameters);
                logger.info("Attempting to restart job " + jobName + " with parameters: " + nextJobParameters);
                jobLauncher.run(job, nextJobParameters);
            } catch (NoSuchJobException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
                logger.error("Failed to restart job", e);
            }
        });
    }

    private String msToHumanDuration(long duration) {
        StringBuilder sb = new StringBuilder();
        for (TimeUnit tu : Arrays.asList(DAYS, HOURS, MINUTES, SECONDS)) {
            long count = tu.convert(duration, MILLISECONDS);
            duration -= tu.toMillis(count);
            if (count > 0) {
                sb.append(String.format("%d %s ", count, tu.toString().substring(0,1).toLowerCase()));
            }
        }
        return sb.toString();
    }
}
