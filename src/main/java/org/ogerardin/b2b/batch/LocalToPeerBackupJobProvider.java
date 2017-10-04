package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.NetworkTarget;
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

@Configuration
public class LocalToPeerBackupJobProvider extends BackupJobBase {

    private static final Log logger = LogFactory.getLog(LocalToPeerBackupJobProvider.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Bean
    @Qualifier("localToPeer")
    protected Job localToPeerBackupJob(Step localToPeerFileProcessingStep)
            throws IOException {
        return jobBuilderFactory.get(LocalToPeerBackupJobProvider.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(localToPeerFileProcessingStep)
                .end()
                .build();
    }

    @Bean
    protected Step localToPeerFileProcessingStep(ItemProcessor<Path, Path> localToPeerItemProcessor, ItemReader<Path> localToPeerReader) throws IOException {
        return stepBuilderFactory.get("fileProcessingStep")
                .<Path, Path>chunk(10)
                .reader(localToPeerReader)
                .processor(localToPeerItemProcessor)
//                .writer(newWriter())
                .build();
    }

    @Bean
    protected ItemProcessor<Path, Path> localToPeerItemProcessor() {
        //TODO do something useful
        return new PassThroughItemProcessor<>();
    }

    @Bean
    @StepScope
    protected ItemReader<Path> localToPeerReader(@Value("#{jobParameters['source.root']}") String root) throws
            IOException {
        return () -> null;
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
                new String[]{"source.root", "target.hostname", "target.port"},
                new String[]{},
                Maps.mapOf(
                        "source.type", FilesystemSource.class.getName(),
                        "target.type", NetworkTarget.class.getName()
                )
        );
    }

}
