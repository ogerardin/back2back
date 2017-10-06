package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.NetworkTarget;
import org.ogerardin.b2b.util.Maps;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

@Configuration
public class LocalToPeerBackupJob extends LocalSourceBackupJob {

    private static final Log logger = LogFactory.getLog(LocalToPeerBackupJob.class);

    @Bean("localToPeerJob")
    protected Job localToPeerBackupJob(Step localToPeerStep)
            throws IOException {
        return jobBuilderFactory.get(LocalToPeerBackupJob.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(localToPeerStep)
                .end()
                .build();
    }

    @Bean("localToPeerStep")
    protected Step step(ItemProcessor<Path, Path> localToPeerItemProcessor, ItemReader<Path> localToPeerReader) throws IOException {
        return stepBuilderFactory.get("processLocalFiles")
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
        //TODO for debug
        return () -> null;
    }

    protected JobParametersValidator validator() {
        JobParametersValidator parentValidator = super.validator();

        JobParametersValidator thisValidator = new BatchJobParametersValidator(
                new String[]{"target.hostname", "target.port"},
                new String[]{},
                Maps.mapOf(
                        "target.type", NetworkTarget.class.getName()
                )
        );

        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(parentValidator, thisValidator));
        return validator;
    }

}
