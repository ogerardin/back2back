package org.ogerardin.b2b.batch;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.entity.BackupSource;
import org.ogerardin.b2b.domain.entity.BackupTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final JobExplorer jobExplorer;
    private final List<Job> allJobs;


    @Autowired
    public JobStarter(BackupSourceRepository sourceRepository, BackupTargetRepository targetRepository, BackupSetRepository backupSetRepository, JobLauncher jobLauncher, JobExplorer jobExplorer, List<Job> allJobs) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
        this.backupSetRepository = backupSetRepository;
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
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

    public void syncJobs() {
        log.debug("SYNCING JOBS");

        // get all source/target combinations that should have a backup job running
        Set<Pair<BackupSource, BackupTarget>> sourceTargetPairs =
                Lists.cartesianProduct(sourceRepository.findAll(), targetRepository.findAll()).stream()
                        .map(l -> new ImmutablePair<>((BackupSource) l.get(0), (BackupTarget) l.get(1)))
                        .filter(p -> p.left.isEnabled())
                        .filter(p -> p.left.shouldStartJob())
                        .filter(p -> p.right.isEnabled())
                        .collect(Collectors.toSet());
        log.debug("Found {} eligible source/target pair(s)", sourceTargetPairs.size());

        // get all corresponding backup sets
        Set<BackupSet> activeBackupSets = new HashSet<>();
        for (Pair<BackupSource, BackupTarget> p : sourceTargetPairs) {
            BackupSource source = p.getLeft();
            BackupTarget target = p.getRight();
            log.debug("Getting backup set for {} / {}", source, target);
            BackupSet backupSet = findBackupSet(source, target);
            activeBackupSets.add(backupSet);
        }
//        log.debug("{} active backup set(s)", activeBackupSets.size());
        Set<String> activeBackupSetIds = activeBackupSets.stream().map(BackupSet::getId).collect(Collectors.toSet());

        //start jobs for all active backup sets, mark others as inactive
        for (BackupSet backupSet : backupSetRepository.findAll()) {
            boolean isActive = activeBackupSetIds.contains(backupSet.getId());
            if (isActive) {
                // backup set is active, make sure the corresponding job is started
                log.debug("Starting job for backup set {}", backupSet.getId());
                startJob(backupSet);
            }
            backupSet.setActive(isActive);
            backupSetRepository.save(backupSet);
        }

        //stop all running jobs not related to an active backupSet
        for (Job job : allJobs) {
            Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(job.getName());
            for (JobExecution jobExecution : runningJobExecutions) {
                String backupSetId = jobExecution.getJobParameters().getString("backupset.id");
                if (!activeBackupSetIds.contains(backupSetId)) {
                    log.debug("job execution {} belongs to inactive backup set, stopping", jobExecution);
                    jobExecution.stop();
                }
            }
        }

        log.debug("Done syncing jobs");
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
    private JobExecution startJob(BackupSource source, BackupTarget target) {
        log.info("Starting job for source:" + source + ", target:" + target);
        BackupSet backupSet = findBackupSet(source, target);
        JobExecution jobExecution = startJob(backupSet);
        return jobExecution;
    }

    /**
     * Retrieve the {@link BackupSet} for the specified source and target; creates it if it doesn't exist.
     */
    private BackupSet findBackupSet(BackupSource source, BackupTarget target) {
        List<BackupSet> backupSets = backupSetRepository.findByBackupSourceAndBackupTarget(source, target);

        if (backupSets.isEmpty()) {
            BackupSet backupSet = new BackupSet();
            backupSet.setBackupSource(source);
            backupSet.setBackupTarget(target);
            backupSetRepository.insert(backupSet);
            log.info("Created backup set: {}", backupSet.getId());
            return backupSet;
        }

        if (backupSets.size() > 1) {
            log.warn("More than 1 backup set found for {}, {}: {}", source, target, backupSets);
        }
        BackupSet backupSet = backupSets.get(0);
        log.debug("Using existing backup set {}", backupSet.getId());
        return backupSet;
    }

    /**
     * Attempts to start the appropriate backup job for the specified BackupSet.
     *
     * @throws JobExecutionException in case Spring Batch failed to start the job
     */
    private JobExecution startJob(BackupSet backupSet) {
        log.debug("Looking for job matching backup set: " + backupSet);

        JobParameters jobParameters = backupSet.getJobParameters();
        log.debug("Parameters: " + jobParameters);

        // try to find a Job that is applicable to the parameters
        Optional<Job> applicableJob = findApplicableJob(jobParameters);
        if (!applicableJob.isPresent()) {
            log.error("No job found for parameters {}", jobParameters);
            return null;
        }
        Job job = applicableJob.get();
        backupSet.setJobName(job.getName());
        backupSetRepository.save(backupSet);

        // launch it
        log.info("Starting job: {}", job.getName());
        try {
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
//            log.debug("Job started: {}", jobExecution);
            return jobExecution;
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn(e.toString());
        } catch (JobExecutionException e) {
            log.error("Exception while trying to start job", e);
        }
        return null;
    }

    /**
     * Finds a Job from all jobs that exist in the Spring application context, that matches the specified parameters
     * i.e. for which calling the validator doesn't throw a {@link JobParametersInvalidException}. The first job to
     * match is returned.
     *
     * @return The Job, or Optional#empty() if not found
     */
    private Optional<Job> findApplicableJob(JobParameters jobParameters) {
        for (Job job : allJobs) {
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
