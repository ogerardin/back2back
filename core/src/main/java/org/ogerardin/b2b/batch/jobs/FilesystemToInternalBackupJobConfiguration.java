package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.ogerardin.b2b.files.md5.StreamingMd5Calculator;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Job configuration for a backup job that processes a source of type {@link FilesystemSource}
 * and backs up to the internal storage (i.e. a {@link StorageService}).
 */
@Configuration
public class FilesystemToInternalBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

    @Autowired
    @Qualifier("gridFsStorageServiceFactory")
    protected StorageServiceFactory storageServiceFactory;

    public FilesystemToInternalBackupJobConfiguration() {
        addStaticParameter("target.type", LocalTarget.class.getName());
    }

    /**
     * Provides a job that performs the backup of a filesystem source into the internal storage.
     */
    @Bean
    protected Job filesystemToInternalBackupJob(
            Step initBatchStep,
            Step computeBatchStep,
            Step backupToInternalStorageStep,
            BackupJobExecutionListener jobListener
    ) {
        return jobBuilderFactory
                .get("filesystemToInternalBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(initBatchStep)               // step 0: init batch
                .next(computeBatchStep)             // step 1: compute files that need to be backed up
                .next(backupToInternalStorageStep)  // step 2: perform backup
                .build();
    }

    @Bean
    @JobScope
    protected Step initBatchStep(
            BackupJobContext jobContext
    ) {
        StorageService storageService = storageServiceFactory.getStorageService(jobContext.getBackupSetId());
        StoredFileVersionInfoProvider storedFileVersionInfoProvider = StoredFileVersionInfoProvider.of(storageService);
        return stepBuilderFactory.get("initBatchStep")
                .tasklet(new InitBatchTasklet(storedFileVersionInfoProvider))
                .build();
    }

    /**
     * Provides a {@link Step} that writes items from the current batch by storing them to the internal storage
     */
    @Bean
    @JobScope
    protected Step backupToInternalStorageStep(
            BackupJobContext jobContext,
            InternalStorageItemWriter internalStorageWriter
    ) {
        return stepBuilderFactory
                .get("backupToInternalStorageStep")
                .<LocalFileInfo, LocalFileInfo> chunk(1) // handle 1 file at a time
                // read files from job context
                .reader(new IteratorItemReader<>(jobContext.getBackupBatch().getFiles()))
                // no processing
                .processor(new PassThroughItemProcessor<>())
                // store them to the internal storage
                .writer(internalStorageWriter)
                .build();
    }


    /**
     * Provides a job-scoped {@link ItemWriter} that stores {@link Path} items into
     * the internal storage
     */
    @Bean
    @JobScope
    protected InternalStorageItemWriter internalStorageItemWriter(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    )  {
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new InternalStorageItemWriter(storageService, properties.getFileThrottleDelay());
    }
    /**
     * Provides a job-scoped {@link org.springframework.batch.item.ItemProcessor} that filters out {@link Path} items
     * corresponding to a file that isn't different from the latest stored version.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor filteringPathItemProcessor(
            @Value("#{jobParameters['backupset.id']}") String backupSetId,
            @Qualifier("springMD5Calculator") StreamingMd5Calculator md5Calculator
    ) {
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        StoredFileVersionInfoProvider storedFileVersionInfoProvider = StoredFileVersionInfoProvider.of(storageService);
        Md5FilteringStrategy filteringStrategy = new Md5FilteringStrategy(storedFileVersionInfoProvider, md5Calculator);
        return new FilteringPathItemProcessor(storedFileVersionInfoProvider, filteringStrategy);
    }


}
