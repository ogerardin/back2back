package org.ogerardin.b2b;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
public class Main {

    private static final Log logger = LogFactory.getLog(Main.class);

    private final BackupSourceRepository sourceRepository;
    private final BackupTargetRepository targetRepository;
    private final BackupSetRepository backupSetRepository;

    private final JobLauncher jobLauncher;
    private final List<Job> allJobs;


    @Autowired
    public Main(BackupSourceRepository sourceRepository,
                BackupTargetRepository targetRepository,
                BackupSetRepository backupSetRepository,
                JobLauncher jobLauncher,
                List<Job> jobs) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
        this.backupSetRepository = backupSetRepository;
        this.jobLauncher = jobLauncher;
        this.allJobs = jobs;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {
            startAllJobs();
        };
    }

    private void startAllJobs() {
        // start jobs for all enabled sources
        sourceRepository.findAll().stream()
                .filter(BackupSource::isEnabled)
                .forEach(this::startJobs);
    }

    private void startJobs(BackupSource source) {
        // start jobs for all enabled targets and specified source
        targetRepository.findAll().stream()
                .filter(BackupTarget::isEnabled)
                .forEach(target -> startJob(source, target));
    }

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

    private void startJob(BackupSet backupSet) throws JobExecutionException, B2BException {
        logger.debug("Looking for job matching backup set: " + backupSet);

        BackupSource source = backupSet.getBackupSource();
        BackupTarget target = backupSet.getBackupTarget();

        // build parameters (delegated to BackupSource and BackupTarget)
        Map<String, JobParameter> params = new HashMap<>();
        backupSet.populateParams(params);
        source.populateParams(params);
        target.populateParams(params);
        JobParameters jobParameters = new JobParameters(params);
        logger.debug("Parameters: " + jobParameters);

        // find Job applicable for parameters
        Job job = findApplicableJob(jobParameters);
        if (job == null) {
            throw new B2BException("No job found for parameters " + jobParameters);
        }

        // launch it
        // FIXME: in case we have a persistent JobRepository, we should find the latest instance with these
        // parameters and apply org.springframework.batch.core.getJobParametersIncrementer.getNext before staring it.
        logger.info("Starting job: " + job.getName() + " with params: " + jobParameters);
        jobLauncher.run(job, jobParameters);
    }

    /**
     * Finds a Job from all known jobs that matches the specified parameters (i.e. for which
     * calling the validator doesn't throw a {@link JobParametersInvalidException}). If several jobs
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
