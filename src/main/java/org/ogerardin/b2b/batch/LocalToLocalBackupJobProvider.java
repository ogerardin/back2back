package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.util.Maps;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LocalToLocalBackupJobProvider extends BackupJobBase {

    private static final Log logger = LogFactory.getLog(LocalToLocalBackupJobProvider.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Bean
    @Qualifier("localToLocal")
    protected Job localToLocalBackupJob(Step localToLocalProcessingStep)
            throws IOException {
        return jobBuilderFactory.get(LocalToLocalBackupJobProvider.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(localToLocalProcessingStep)
                .end()
                .build();
    }

    @Bean
    protected Step localToLocalProcessingStep(ItemProcessor<Path, Path> localToLocalItemProcessor, ItemReader<Path> localToLocalItemReader) throws IOException {
        return stepBuilderFactory.get("localToLocalProcessingStep")
                .<Path, Path> chunk(10)
                .reader(localToLocalItemReader)
                .processor(localToLocalItemProcessor)
//                .writer(newWriter())
                .build();
    }

    @Bean
    protected ItemProcessor<Path, Path> localToLocalItemProcessor() {
        //TODO do something useful
        return new PassThroughItemProcessor<>();
    }

    @Bean
    @StepScope
    protected ItemReader<Path> localToLocalItemReader(@Value("#{jobParameters['source.root']}") String rootParam)
            throws IOException {
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

    private BatchJobParametersValidator validator() {
        return new BatchJobParametersValidator(
                new String[]{"source.root", "target.path"},
                new String[]{},
                Maps.mapOf(
                        "source.type", FilesystemSource.class.getName(),
                        "target.type", LocalTarget.class.getName()
                )
        );
    }




}
