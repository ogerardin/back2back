package org.ogerardin.b2b;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.batch.BackupJobBuilder;
import org.ogerardin.b2b.config.BackupSourceRepository;
import org.ogerardin.b2b.config.BackupTargetRepository;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
public class Main {

    private static final Log logger = LogFactory.getLog(Main.class);

    private final BackupSourceRepository sourceRepository;
    private final BackupTargetRepository targetRepository;
    private final BackupJobBuilder backupJobBuilder;
    private final JobLauncher jobLauncher;


    @Autowired
    public Main(BackupSourceRepository sourceRepository,
                BackupTargetRepository targetRepository,
                @Qualifier("proxyBackupJobBuilder") BackupJobBuilder backupJobBuilder,
                JobLauncher jobLauncher) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
        this.backupJobBuilder = backupJobBuilder;
        this.jobLauncher = jobLauncher;
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
            } catch (IOException | JobExecutionException | B2BException | InstantiationException | NoSuchMethodException e) {
                logger.error("Failed to start job for " + source + ", " + target, e);
            }
        });
    }

    private void startJob(BackupSource source, BackupTarget target) throws IOException, JobExecutionException, B2BException, InstantiationException, NoSuchMethodException {
        Job job = backupJobBuilder.newBackupJob(source, target);
        jobLauncher.run(job, new JobParameters());

    }


}
