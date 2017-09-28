package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.files.RecursivePathCollector;
import org.ogerardin.b2b.worker.SingleFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableBatchProcessing
public class BatchConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfigurationProvider.class);

    private final JobBuilderFactory jobBuilderFactory;
    private final JobLauncher jobLauncher;
    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    public BatchConfigurationProvider(JobBuilderFactory jobBuilderFactory,
                                      @Qualifier("asyncJobLauncher") JobLauncher asyncJobLauncher,
                                      StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobLauncher = asyncJobLauncher;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @Qualifier("asyncJobLauncher")
    static JobLauncher asyncJobLauncher(AsyncTaskExecutor asyncTaskExecutor, JobRepository jobRepository) {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setTaskExecutor(asyncTaskExecutor);
        simpleJobLauncher.setJobRepository(jobRepository);
        return simpleJobLauncher;
    }

    private static ListItemReader<Path> newReader() throws IOException {
        Path root = Paths.get(System.getProperty("user.home"));
        logger.info("Collecting all files under " + root);
        RecursivePathCollector pathCollector = new RecursivePathCollector(root);
        pathCollector.walkTree();
        Collection<Path> paths = pathCollector.getPaths();
        logger.info("Found " + paths.size() + " files");

        return new ListItemReader<>(new ArrayList<>(paths));
    }

    private static ItemProcessor<Path, Path> newProcessor(SingleFileProcessor fileProcessor) {
        return item -> {
            fileProcessor.process(item.toFile());
            return item;
        };
    }

    private Job newBackupJob(SingleFileProcessor fileProcessor, JobExecutionListener listener) throws IOException {
        return jobBuilderFactory.get("backupJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(newFileProcessingStep(fileProcessor))
                .end()
                .build();
    }

    private Step newFileProcessingStep(SingleFileProcessor fileProcessor) throws IOException {
        return stepBuilderFactory.get("fileProcessingStep")
                .<Path, Path> chunk(10)
                .reader(newReader())
                .processor(newProcessor(fileProcessor))
//                .writer(newWriter())
                .build();
    }



    private JobExecutionListener listener() {
        return new JobExecutionListenerSupport() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                logger.info("About to start FAKE backup job");
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                logger.info("FAKE backup job terminated with status: " + jobExecution.getStatus());
            }
        };
    }

    public JobExecution startBackupJob(SingleFileProcessor fileProcessor) throws IOException, JobExecutionException {
        Job job = newBackupJob(fileProcessor, listener());
        return jobLauncher.run(job, new JobParameters());
    }
}
