package org.ogerardin.b2b.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

//@Configuration
//@EnableBatchProcessing
public class BackupBatchConfiguration {

    Logger logger = LoggerFactory.getLogger(BackupBatchConfiguration.class);

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    public ListItemReader<Path> reader() throws URISyntaxException, IOException {
        Path root = Paths.get("/Users/olivier");
        List<Path> pathList = Files.walk(root).collect(Collectors.toList());

        return new ListItemReader<>(pathList);
    }

    @Bean
    public ItemProcessor<Path, Path> processor() {
        return new PassThroughItemProcessor<>();
    }

    @Bean
    public ItemWriter<Path> writer() {
        return items -> items.forEach(
                path -> logger.info("FAKE backup " + path)
        );
    }

    @Bean
    public Job backupJob(JobExecutionListener listener) throws IOException, URISyntaxException {
        return jobBuilderFactory.get("backupJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() throws IOException, URISyntaxException {
        return stepBuilderFactory.get("step1")
                .<Path, Path> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public JobExecutionListener listenr() {
        return new JobExecutionListenerSupport() {
            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    logger.info("FAKE backup done!");
                }
            }
        };
    }
}
