package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LocalToLocalBackupJob extends LocalSourceBackupJob {

    private static final Log logger = LogFactory.getLog(LocalToLocalBackupJob.class);

    @Autowired
    StorageService storageService;

    public LocalToLocalBackupJob() {
        addStaticParameter("target.type", LocalTarget.class.getName());
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
    @StepScope
    protected ItemProcessor<Path, Path> itemProcessor(
            @Value("#{jobParameters['target.path']}") String targetPathParam,
            @Value("#{jobParameters['source.root']}") String sourceRootParam,
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {

        return itemPath -> {
            logger.debug("Processing " + itemPath);
            try {
                storageService.store(itemPath);
            } catch (Exception e) {
                logger.error("Failed to store file: " + itemPath, e);
            }
            Thread.sleep(1000);
            return itemPath;
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
