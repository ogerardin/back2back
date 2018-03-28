package org.ogerardin.b2b.batch;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Class responsible for starting appropriate {@link Job}s based on the stored configuration.
 * How this works:
 * -each source/target combination for all configured {@link BackupSource}s and {@link BackupTarget}s is examined
 * -for each pair, the corresponding {@link BackupSet} is retrieved or created if it didn't exist.
 * -a {@link JobParameters} is populated from this BackupSet
 * -each configured {@link Job} is examined, if the JobParameters is valid for this job, then it is started with
 * these params
 */
@Component
@Slf4j
public class JobStarter {

    private final BackupSourceRepository sourceRepository;
    private final BackupTargetRepository targetRepository;
    private final BackupSetRepository backupSetRepository;

    private final JobLauncher jobLauncher;
    private final List<Job> allJobs;


    @Autowired
    public JobStarter(BackupSourceRepository sourceRepository, BackupTargetRepository targetRepository, BackupSetRepository backupSetRepository, JobLauncher jobLauncher, List<Job> allJobs) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
        this.backupSetRepository = backupSetRepository;
        this.jobLauncher = jobLauncher;
        this.allJobs = allJobs;
    }

    /**
     * Attempt to start jobs for all enabled sources
     */
    public void startAllJobs() {
        log.debug("STARTING JOBS");
        for (BackupSource backupSource : sourceRepository.findAll()) {
            if (backupSource.shouldStartJob() && backupSource.isEnabled()) {
                startJobs(backupSource);
            }
        }
        log.debug("Done strating jobs");
    }

    /**
     * Attempt to start backup jobs for all enabled targets and the specified source
     */
    private void startJobs(BackupSource source) {
        log.debug("Starting jobs for source " + source);
        for (BackupTarget target : targetRepository.findAll()) {
            if (target.isEnabled()) {
                startJob(source, target);
            }
        }
    }

    /**
     * Attempt to start a backup job for the specified source and target
     */
    private void startJob(BackupSource source, BackupTarget target) {
        log.debug("Starting job for source:" + source + ", target:" + target);
        try {
            BackupSet backupSet = findBackupSet(source, target);
            startJob(backupSet);
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn(e.toString());
        } catch ( JobExecutionException | B2BException e) {
            log.error("Failed to start job for " + source + ", " + target, e);
        }
    }

    /**
     * Retrieve the {@link BackupTarget} for the specified source and target; creates it if it doesn't exist.
     */
    private BackupSet findBackupSet(BackupSource source, BackupTarget target) {
        List<BackupSet> backupSets = backupSetRepository.findByBackupSourceAndBackupTarget(source, target);

        if (backupSets.isEmpty()) {
            log.info("Creating backup set for " + source + ", " + target);
            BackupSet backupSet = new BackupSet();
            backupSet.setBackupSource(source);
            backupSet.setBackupTarget(target);
            backupSetRepository.insert(backupSet);
            return backupSet;
        }

        if (backupSets.size() > 1) {
            log.error("More than 1 backup set found for " + source + ", " + target);
        }
        return backupSets.get(0);
    }

    /**
     * Attempts to start the appropriate backup job for the specified BackupSet.
     * @throws JobExecutionException in case Spring Batch failed to start the job
     */
    private void  startJob(BackupSet backupSet) throws JobExecutionException, B2BException {
        log.debug("Looking for job matching backup set: " + backupSet);

        JobParameters jobParameters = backupSet.getJobParameters();
        log.debug("Parameters: " + jobParameters);

        // try to find a Job that is applicable to the parameters
        Job job = findApplicableJob(jobParameters).orElseThrow(() ->
            new B2BException("No job found for parameters " + jobParameters));

        // launch it
        log.info("Starting job: " + job.getName() + " with params: " + jobParameters);
        JobExecution jobExecution = jobLauncher.run(job, jobParameters);
        backupSet.setJobName(job.getName());
        backupSet.setJobId(jobExecution.getJobId());
        backupSetRepository.save(backupSet);
    }

    /**
     * Finds a Job from all jobs that exist in the Spring application context, which matches the specified parameters
     * i.e. for which calling the validator doesn't throw a {@link JobParametersInvalidException}. The first job to
     * match is returned.
     * @return The Job, or Optional#empty() if not found
     */
    private Optional<Job> findApplicableJob(JobParameters jobParameters) {
        for (Job job: allJobs) {
            JobParametersValidator parametersValidator = job.getJobParametersValidator();
            try {
                parametersValidator.validate(jobParameters);
                // validation succeeded: use this job
                return Optional.of(job);
            } catch (JobParametersInvalidException e) {
                // validation failed
            }
        }
        // none of the jobs passed validation
        return Optional.empty();
    }


}
