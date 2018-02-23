package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class JobStarter {

    private static final Log logger = LogFactory.getLog(JobStarter.class);

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
        sourceRepository.findAll().stream()
                .filter(BackupSource::isEnabled)
                .forEach(this::startJobs);
    }

    /**
     * Attempt to start backup jobs for all enabled targets and the specified source
     */
    private void startJobs(BackupSource source) {
        targetRepository.findAll().stream()
                .filter(BackupTarget::isEnabled)
                .forEach(target -> startJob(source, target));
    }

    /**
     * Attempt to start a backup job for the specified source and target
     */
    private void startJob(BackupSource source, BackupTarget target) {
        try {
            BackupSet backupSet = findBackupSet(source, target);
            startJob(backupSet);
        } catch (JobExecutionAlreadyRunningException e) {
            logger.warn(e.toString());
        } catch ( JobExecutionException | B2BException e) {
            logger.error("Failed to start job for " + source + ", " + target, e);
        }
    }

    /**
     * Retrieve the {@link BackupTarget} for the specified source and target; creates it if it doesn't exist.
     */
    private BackupSet findBackupSet(BackupSource source, BackupTarget target) {
        List<BackupSet> backupSets = backupSetRepository.findByBackupSourceAndBackupTarget(source, target);

        if (backupSets.isEmpty()) {
            logger.info("Creating backup set for " + source + ", " + target);
            BackupSet backupSet = new BackupSet();
            backupSet.setBackupSource(source);
            backupSet.setBackupTarget(target);
            backupSetRepository.insert(backupSet);
            return backupSet;
        }

        if (backupSets.size() > 1) {
            logger.error("More than 1 backup set found for " + source + ", " + target);
        }
        return backupSets.get(0);
    }

    /**
     * Attempts to start the appropriate backup job for the specified BackupSet.
     * @throws JobExecutionException in case Spring Batch failed to start the job
     */
    private void startJob(BackupSet backupSet) throws JobExecutionException, B2BException {
        logger.debug("Looking for job matching backup set: " + backupSet);

        Map<String, JobParameter> params = new HashMap<>();
        backupSet.populateParams(params);
        JobParameters jobParameters = new JobParameters(params);
        logger.debug("Parameters: " + jobParameters);

        // try to find a Job that is applicable to the parameters
        Job job = findApplicableJob(jobParameters);
        if (job == null) {
            throw new B2BException("No job found for parameters " + jobParameters);
        }

        // launch it
        // FIXME: in case we have a persistent JobRepository, we should find the latest instance with these
        // FIXME parameters and apply org.springframework.batch.core.getJobParametersIncrementer.getNext before staring
        logger.info("Starting job: " + job.getName() + " with params: " + jobParameters);
        jobLauncher.run(job, jobParameters);
    }

    /**
     * Finds a Job from all jobs that exist in the Spring application context, which matches the specified parameters
     * i.e. for which calling the validator doesn't throw a {@link JobParametersInvalidException}. If several jobs
     * match, which one is returned is undefined.
     * @return The Job, or null if none found
     */
    private Job findApplicableJob(JobParameters jobParameters) {
        for (Job job: allJobs) {
            JobParametersValidator parametersValidator = job.getJobParametersValidator();
            if (parametersValidator != null) {
                try {
                    parametersValidator.validate(jobParameters);
                } catch (JobParametersInvalidException e) {
                    // validation failed: try next job
                    continue;
                }
            }
            // validation succeeded: use this job
            return job;
        }
        // none of the jobs passed validation
        return null;
    }


}
