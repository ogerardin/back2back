package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LocalToLocalBackupJob extends BackupJobBase {

    private static final Log logger = LogFactory.getLog(LocalToLocalBackupJob.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Bean
    protected Job backupJob(Step fileProcessingStep) throws IOException {
        return jobBuilderFactory.get(getClass().getSimpleName())
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(fileProcessingStep)
                .end()
                .build();
    }

    @Bean
    protected Step fileProcessingStep(ItemProcessor<Path, Path> itemProcessor, ItemReader<Path> reader) throws IOException {
        return stepBuilderFactory.get("fileProcessingStep")
                .<Path, Path> chunk(10)
                .reader(reader)
                .processor(itemProcessor)
//                .writer(newWriter())
                .build();
    }

    @Bean
    protected ItemProcessor<Path, Path> itemProcessor() {
        //TODO do something useful
        return new PassThroughItemProcessor<>();
    }

    @Bean
    @StepScope
    protected static ItemReader<Path> reader(@Value("#{jobParameters['root']}") String rootParam) throws IOException {
        Path root = Paths.get(rootParam);
        return new FileTreeWalkerItemReader(root);
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



}
