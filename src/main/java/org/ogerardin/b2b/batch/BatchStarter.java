package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.worker.SingleFileProcessor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class BatchStarter {

    private static final Log logger = LogFactory.getLog(BatchStarter.class);

    private final JobBuilderFactory jobBuilderFactory;
    private final JobLauncher jobLauncher;
    private final StepBuilderFactory stepBuilderFactory;


    @Autowired
    public BatchStarter(JobBuilderFactory jobBuilderFactory, JobLauncher jobLauncher, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobLauncher = jobLauncher;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    private static ItemReader<Path> newReader() throws IOException {
        Path root = Paths.get(System.getProperty("user.home"));
        return new FileTreeWalkerItemReader(root);
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
