package org.ogerardin.b2b.batch;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ogerardin.b2b.B2BProperties;
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
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
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

    @Autowired
    B2BProperties properties;

    private final BackupSourceRepository sourceRepository;
    private final BackupTargetRepository targetRepository;
    private final BackupSetRepository backupSetRepository;

    @Autowired
    private JobRepository jobRepository;

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
     * Attempts to sync running jobs with configured sources and targets
     */
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
        Set<BackupSet> activeBackupSets = sourceTargetPairs.stream()
                .peek(st -> log.debug("Getting backup set for {} / {}", st.getLeft(), st.getRight()))
                .map(st -> findBackupSet(st.getLeft(), st.getRight()))
                .collect(Collectors.toSet());

        Set<String> activeBackupSetIds = activeBackupSets.stream()
                .map(BackupSet::getId)
                .collect(Collectors.toSet());

        //start jobs for all active backup sets, mark others as inactive
        Set<JobExecution> validExecutions = new HashSet<>();
        for (BackupSet backupSet : backupSetRepository.findAll()) {
            log.debug("Processing backup set {}", backupSet);
            String backupSetId = backupSet.getId();
            boolean isActive = activeBackupSetIds.contains(backupSetId);

            if (!isActive) {
                log.debug("Backup set {} is not active", backupSetId);
                // Stop any running job associated to this backup set
                stopAllJobs(backupSet);
                backupSet.setNextBackupTime(null);
                backupSet.setStatus("Inactive");
                backupSetRepository.save(backupSet);
                continue;
            }

            // Backup set is active, we need to make sure it's running or scheduled

            log.debug("Looking for matching job");
            JobParameters jobParameters = backupSet.getJobParameters();
            log.debug("Parameters: {}", jobParameters);
            // try to find a Job that is applicable to the parameters
            Optional<Job> applicableJob = findApplicableJob(jobParameters);
            if (!applicableJob.isPresent()) {
                String msg = String.format("No applicable job found for parameters %s", jobParameters);
                log.error(msg);
                //TODO better error reporting for this case
                backupSet.setStatus(String.format("Failed: %s", msg));
                backupSetRepository.save(backupSet);
                continue;
            }
            Job job = applicableJob.get();
            log.debug("Found applicable job: {}", job);
            backupSet.setJobName(job.getName());

            // Get last execution of job with those parameters
            JobExecution lastJobExecution = findLastJobExecution(job.getName(), jobParameters);
            log.debug("last execution: {}", lastJobExecution);

            // Check if running
            if (lastJobExecution != null && lastJobExecution.isRunning()) {
                log.debug("Backup set is active and job is running, nothing to do here");
                validExecutions.add(lastJobExecution);
                continue;
            }

            // Backup set is active but no appropriate job is running

            // Stop any running job associated to this backup set (in case parameters have changed)
            stopAllJobs(backupSet);

            // if we have reached next scheduled backup time, start the job
            Instant nextBackupTime = backupSet.getNextBackupTime();
            log.debug("Backup set {} scheduled to start at {}", backupSetId, nextBackupTime);
            if (nextBackupTime == null || Instant.now().isAfter(nextBackupTime)) {
                log.debug("Starting job for backup set {}", backupSetId);

                // If we had a previous execution, use incrementer to generate new parameter set
                if (lastJobExecution != null) {
                    JobParameters lastJobParameters = lastJobExecution.getJobParameters();
                    jobParameters = job.getJobParametersIncrementer().getNext(lastJobParameters);
                    log.debug("Incremented parameters: {}", jobParameters);
                }

                try {
                    JobExecution jobExecution = jobLauncher.run(job, jobParameters);
                    validExecutions.add(jobExecution);
                } catch (JobExecutionAlreadyRunningException | JobRestartException | JobParametersInvalidException | JobInstanceAlreadyCompleteException e) {
                    String msg = String.format("Failed to start job: %s", e.toString());
                    backupSet.setStatus(msg);
                    backupSetRepository.save(backupSet);
                    log.error("Failed to start job", e);
                }
            }
            backupSetRepository.save(backupSet);
        }

        //stop all remaining running jobs
        allJobs.stream()
                .map(j -> jobExplorer.findRunningJobExecutions(j.getName()))
                .flatMap(Collection::stream)
                .filter(je -> !validExecutions.contains(je))
                .forEach(je -> {
                    log.debug("Stopping stale job execution {}", je);
                    je.stop();
                }
        );

        log.debug("Done syncing jobs");
    }

    /**
     * Returns the last {@link JobExecution} (by start date) across all {@link JobInstance}s of the job with the
     * specified name whose job parameters contain all the paramaters of the specified {@link JobParameters}
     */
    private JobExecution findLastJobExecution(String jobName, JobParameters jobParameters) {
        return jobExplorer.findJobInstancesByJobName(jobName, 0, Integer.MAX_VALUE).stream()
                .map(jobExplorer::getJobExecutions)
                .flatMap(Collection::stream)
                .filter(jobExecution -> containsAllParameters(jobExecution, jobParameters))
                .max(Comparator.comparing(JobExecution::getStartTime))
                .orElse(null);
    }

    private boolean containsAllParameters(JobExecution jobExecution, JobParameters jobParameters) {
        val jobExecutionParameterMap = jobExecution.getJobParameters().getParameters();
        val targetParameterMap = jobParameters.getParameters();
        return jobExecutionParameterMap.entrySet().containsAll(targetParameterMap.entrySet());
    }

    private void stopAllJobs(BackupSet backupSet) {
        String jobName = backupSet.getJobName();
        String backupSetId = backupSet.getId();
        jobExplorer.findRunningJobExecutions(jobName).stream()
                .filter(je -> backupSetId.equals(je.getJobParameters().getString("backupset.id")))
                .forEach(je -> {
                    log.debug("Stopping outdated job execution {} for backup set {}", je, backupSetId);
                    je.stop();
                });
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
