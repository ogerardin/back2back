package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

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
        Instant completeTime = jobExecution.getEndTime().toInstant();
        backupSet.setLastBackupCompleteTime(completeTime);
        backupSet.setBatchCount(0);
        backupSet.setBatchSize(0);
        String status = "Complete";

        if (properties != null && properties.isContinuousBackup()) {
            // Schedule a job restart. This is done asynchronously because
            // the current job is not considered as complete until we exit this function.
            long pauseAfterBackup = properties.getPauseAfterBackup();
            Instant nextBackupTime = completeTime.plusMillis(pauseAfterBackup);
            backupSet.setNextBackupTime(nextBackupTime);
            status += " (next backup at " + nextBackupTime + ")";
            scheduleDelayedRestart(jobExecution, pauseAfterBackup);
        }
        backupSet.setStatus(status);

        backupSetRepository.save(backupSet);
    }

    private void scheduleDelayedRestart(JobExecution jobExecution, long delay) {
        asyncTaskExecutor.submit(() -> {
            String jobName = jobExecution.getJobInstance().getJobName();
            JobParameters jobParameters = jobExecution.getJobParameters();
            // pause
            try {
                log.info("Pausing for " + msToHumanDuration(delay));
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.warn("Restart task interrupted during pause: " + e.toString());
            }
            // restart job
            try {
                Job job = jobLocator.getJob(jobName);
                JobParameters nextJobParameters = job.getJobParametersIncrementer().getNext(jobParameters);
                log.info("Attempting to restart job " + jobName + " with parameters: " + nextJobParameters);
                jobLauncher.run(job, nextJobParameters);
            } catch (NoSuchJobException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobInstanceAlreadyCompleteException | JobRestartException e) {
                log.error("Failed to restart job", e);
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
