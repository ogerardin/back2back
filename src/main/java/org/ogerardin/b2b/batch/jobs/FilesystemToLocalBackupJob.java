package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.batch.FileTreeWalkerItemReader;
import org.ogerardin.b2b.batch.PathItemProcessListener;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.files.MD5Calculator;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StoredFileInfo;
import org.ogerardin.b2b.storage.gridfs.GridFsStorageProvider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FilesystemToLocalBackupJob extends FilesystemSourceBackupJob {

    private static final Log logger = LogFactory.getLog(FilesystemSourceBackupJob.class);

    @Autowired
    MD5Calculator md5Calculator;

    @Autowired
    B2BProperties properties;

    @Autowired
    private MongoDbFactory mongoDbFactory;

    @Autowired
    private MongoConverter mongoConverter;

    public FilesystemToLocalBackupJob() {
        addStaticParameter("target.type", LocalTarget.class.getName());
    }

    @Bean("localToLocalJob")
    protected Job job(Step localToLocalStep, JobExecutionListener jobListener) {
        return jobBuilderFactory.get(FilesystemSourceBackupJob.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .flow(localToLocalStep)
                .end()
                .build();
    }

    @Bean("localToLocalStep")
    protected Step step(ItemProcessor<Path, Path> localToLocalItemProcessor, ItemReader<Path> localToLocalItemReader, PathItemProcessListener itemProcessListener) {
        return stepBuilderFactory.get("processLocalFiles")
                .<Path, Path> chunk(10)
                .reader(localToLocalItemReader)
                .processor(localToLocalItemProcessor)
//                .writer(newWriter())
                .listener(itemProcessListener)
                .build();
    }

    @Bean(name = "localToLocalItemProcessor")
    @StepScope
    protected ItemProcessor<Path, Path> itemProcessor(
            @Value("#{jobParameters['source.root']}") String sourceRootParam,
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        // we use the backupSetId as bucket name for GridFS so that all the files backed up as part of a backupSet
        // are stored in a distinct bucket
        // TODO we should implement a maintenance job to delete buckets for which there is no backupSet
        StorageService storageService = new GridFsStorageProvider(mongoDbFactory, mongoConverter, backupSetId);
        return new LocalStorageItemProcessor(storageService);
    }

    @Bean
    @StepScope
    protected ItemReader<Path> localToLocalItemReader(@Value("#{jobParameters['source.root']}") String rootParam) {
        Path root = Paths.get(rootParam);
        return new FileTreeWalkerItemReader(root);
    }

    private class LocalStorageItemProcessor implements ItemProcessor<Path, Path> {
        private final StorageService storageService;

        public LocalStorageItemProcessor(StorageService storageService) {
            this.storageService = storageService;
        }

        @Override
        public Path process(Path itemPath) throws Exception {
            logger.debug("Processing " + itemPath);

            try {
                StoredFileInfo info = storageService.query(itemPath);
                String storedMd5hash = info.getMd5hash();

                if (storedMd5hash != null) {
                    byte[] bytes = Files.readAllBytes(itemPath);
                    String computedMd5Hash = md5Calculator.hexMd5Hash(bytes);
                    if (computedMd5Hash.equalsIgnoreCase(storedMd5hash)) {
                        logger.debug("  Hash unchanged, skipping file");
                        return itemPath;
                    }
                }
            } catch (StorageFileNotFoundException e) {
                // file not stored yet, proceed
            }

            try {
                storageService.store(itemPath);
            } catch (Exception e) {
                logger.error("Failed to store file: " + itemPath, e);
            }
            if (properties.getThrottleDelay() != 0) {
                Thread.sleep(properties.getThrottleDelay());
            }
            return itemPath;
        }
    }
}
