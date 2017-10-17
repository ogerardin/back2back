package org.ogerardin.b2b;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.ogerardin.b2b.storage.StorageService;
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
import org.springframework.data.domain.Example;

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
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.init();

            startAllJobs();
        };
    }

    private void startAllJobs() {
        sourceRepository.findAll().forEach(this::startJobs);
    }

    private void startJobs(BackupSource source) {
        targetRepository.findAll().forEach(target -> {
            try {
                BackupSet backupSet = findBackupSet(source, target);
                startJob(backupSet);
            } catch (JobExecutionAlreadyRunningException e) {
                logger.warn(e.toString());
            } catch ( JobExecutionException | B2BException e) {
                logger.error("Failed to start job for " + source + ", " + target, e);
            }
        });
    }

    private BackupSet findBackupSet(BackupSource source, BackupTarget target) {
        BackupSet backupSet = new BackupSet();
        backupSet.setBackupSourceId(source.getId());
        backupSet.setBackupTargetId(target.getId());
        List<BackupSet> backupSets = backupSetRepository.findAll(Example.of(backupSet));

        if (backupSets.isEmpty()) {
            logger.info("No backup set found for " + source + ", " + target + ": creating one");
            backupSetRepository.insert(backupSet);
            return backupSet;
        }

        if (backupSets.size() > 1) {
            logger.error("More than 1 backup set found for " + source + ", " + target);
        }
        return backupSets.get(0);
    }

    private void startJob(BackupSet backupSet) throws JobExecutionException, B2BException {
        logger.debug("Finding job for backup set " + backupSet);

        BackupSource source = sourceRepository.findOne(backupSet.getBackupSourceId());
        BackupTarget target = targetRepository.findOne(backupSet.getBackupTargetId());

        // build parameters (delegated to BackupSource and BackupTarget)
        Map<String, JobParameter> params = new HashMap<>();
        backupSet.populateParams(params);
        source.populateParams(params);
        target.populateParams(params);
        JobParameters jobParameters = new JobParameters(params);

        // find Job applicable for parameters
        Job applicableJob = findApplicableJob(jobParameters);
        if (applicableJob == null) {
            throw new B2BException("no job applicable for parameters " + jobParameters);
        }

        // launch it
        logger.debug("Found applicable job, starting it: " + applicableJob);
        jobLauncher.run(applicableJob, jobParameters);
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
            // validation suceeded: use this job
            return job;
        }
        // no applicable job
        return null;
    }


}
