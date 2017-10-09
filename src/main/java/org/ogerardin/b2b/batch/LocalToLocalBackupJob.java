package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.LocalTarget;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LocalToLocalBackupJob extends LocalSourceBackupJob {

    private static final Log logger = LogFactory.getLog(LocalToLocalBackupJob.class);


    public LocalToLocalBackupJob() {
        addStaticParameter("target.type", LocalTarget.class.getName());
        addMandatoryParameter("target.path");
    }

    @Bean("localToLocalJob")
    protected Job job(Step localToLocalStep) throws IOException {
        return jobBuilderFactory.get(LocalToLocalBackupJob.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(localToLocalStep)
                .end()
                .build();
    }

    @Bean("localToLocalStep")
    protected Step step(ItemProcessor<Path, Path> localToLocalItemProcessor, ItemReader<Path> localToLocalItemReader) throws IOException {
        return stepBuilderFactory.get("processLocalFiles")
                .<Path, Path> chunk(10)
                .reader(localToLocalItemReader)
                .processor(localToLocalItemProcessor)
//                .writer(newWriter())
                .build();
    }

    @Bean(name = "localToLocalItemProcessor")
    protected ItemProcessor<Path, Path> itemProcessor() {
        return item -> {
            logger.debug("Processing " + item);
            Thread.sleep(1000);
            return item;
        };
    }

    @Bean
    @StepScope
    protected ItemReader<Path> localToLocalItemReader(@Value("#{jobParameters['source.root']}") String rootParam)
            throws IOException {
        Path root = Paths.get(rootParam);
        return new FileTreeWalkerItemReader(root);
    }

}
