package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.backup.SingleFileProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Abstract superclass for implementors of {@link BackupJobBuilder}
 */
@Component
public abstract class BackupJobBuilderBase {

    private static final Log logger = LogFactory.getLog(BackupJobBuilderBase.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    protected static ItemReader<Path> newReader() throws IOException {
        Path root = Paths.get(System.getProperty("user.home"));
        return new FileTreeWalkerItemReader(root);
    }

    protected ItemProcessor<Path, Path> newProcessor(SingleFileProcessor fileProcessor) {
        return item -> {
            fileProcessor.process(item.toFile());
            return item;
        };
    }

    protected Job newBackupJob(String name, SingleFileProcessor fileProcessor, JobExecutionListener listener) throws IOException {
        return jobBuilderFactory.get(name)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(newFileProcessingStep(fileProcessor))
                .end()
                .build();
    }

    protected Step newFileProcessingStep(SingleFileProcessor fileProcessor) throws IOException {
        return stepBuilderFactory.get("fileProcessingStep")
                .<Path, Path> chunk(10)
                .reader(newReader())
                .processor(newProcessor(fileProcessor))
//                .writer(newWriter())
                .build();
    }



    protected JobExecutionListener listener() {
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

}
