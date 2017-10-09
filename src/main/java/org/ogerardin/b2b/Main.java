package org.ogerardin.b2b;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.config.BackupSourceRepository;
import org.ogerardin.b2b.config.BackupTargetRepository;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
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

import java.io.IOException;
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

    private final JobLauncher jobLauncher;
    private final List<Job> allJobs;


    @Autowired
    public Main(BackupSourceRepository sourceRepository,
                BackupTargetRepository targetRepository,
                JobLauncher jobLauncher,
                List<Job> jobs) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
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

    private void startAllJobs() throws JobExecutionException, IOException {
        sourceRepository.findAll().forEach(this::startJobs);
    }

    private void startJobs(BackupSource source) {
        targetRepository.findAll().forEach(target -> {
            try {
                startJob(source, target);
            } catch (JobExecutionAlreadyRunningException e) {
                logger.warn(e.toString());
            } catch (IOException | JobExecutionException | B2BException | InstantiationException | NoSuchMethodException e) {
                logger.error("Failed to start job for " + source + ", " + target, e);
            }
        });
    }

    private void startJob(BackupSource source, BackupTarget target) throws IOException, JobExecutionException, B2BException, InstantiationException, NoSuchMethodException {

        logger.debug("Finding job for " + source + ", " + target);

        // build parameters (delegated to BackupSource and BackupTarget)
        Map<String, JobParameter> params = new HashMap<>();
        source.populateParams(params);
        target.populateParams(params);
        JobParameters jobParameters = new JobParameters(params);

        // find Job applicable for parameters
        Job applicableJob = getApplicableJob(jobParameters);
        if (applicableJob == null) {
            throw new B2BException("no job applicable for parameters " + jobParameters);
        }
        logger.debug("Found applicable job: " + applicableJob);

        // launch it
        logger.debug("Starting job: " + applicableJob);
        jobLauncher.run(applicableJob, jobParameters);
    }

    private Job getApplicableJob(JobParameters jobParameters) {
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
