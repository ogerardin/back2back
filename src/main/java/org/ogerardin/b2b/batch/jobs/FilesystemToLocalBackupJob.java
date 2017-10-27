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
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.ogerardin.b2b.storage.StoredFileInfo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Job implementation for a backup job that process a source of type {@link org.ogerardin.b2b.domain.FilesystemSource}
 * and backup to a local destination (i.e. a {@link StorageService}).
 */
@Configuration
public class FilesystemToLocalBackupJob extends FilesystemSourceBackupJob {

    private static final Log logger = LogFactory.getLog(FilesystemSourceBackupJob.class);

    @Autowired
    MD5Calculator md5Calculator;

    @Autowired
    B2BProperties properties;

    @Autowired
    @Qualifier("gridFsStorageServiceFactory")
    private StorageServiceFactory storageServiceFactory;

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
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        // we use the backupSetId as storage service name; for GridFsStorageService this translates to the
        // bucket name used by GridFS so that all the files backed up as part of a backupSet are stored in a
        // distinct bucket
        // TODO we should implement a maintenance job to delete buckets for which there is no backupSet
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new LocalStorageItemProcessor(storageService);
    }

    @Bean
    @StepScope
    protected ItemReader<Path> localToLocalItemReader(
            @Value("#{jobParameters['source.root']}") String sourceRootParam
    ) {
        Path root = Paths.get(sourceRootParam);
        return new FileTreeWalkerItemReader(root);
    }

    /**
     * ItemProcessor implementation that performs backup of a {@link Path} item to a StorageService.
     * The file is only stored if it hasn't been stored yet or the locally computed MD5 hash is different from the
     * stored file's MD5 hash.
     */
    private class LocalStorageItemProcessor implements ItemProcessor<Path, Path> {
        private final StorageService storageService;

        LocalStorageItemProcessor(StorageService storageService) {
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
