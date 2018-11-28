package org.ogerardin.b2b.batch.jobs;

import lombok.val;
import org.ogerardin.b2b.batch.jobs.listeners.BackupJobExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.ComputeBatchStepExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.FileBackupListener;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.batch.jobs.support.HashFilteringStrategy;
import org.ogerardin.b2b.batch.jobs.support.StorageServiceLatestStoredRevisionProviderAdapter;
import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.LocalTarget;
import org.ogerardin.b2b.hash.InputStreamHashCalculator;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.Arrays;

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
     * A job that performs the backup of a filesystem source into the internal storage.
     */
    @Bean
    protected Job filesystemToInternalBackupJob(
            Step internalInitBatchStep,
            Step internalComputeBatchStep,
            Step backupToInternalStorageStep,
            BackupJobExecutionListener jobListener
    ) {
        return jobBuilderFactory
                .get("filesystemToInternalBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(internalInitBatchStep)       // step 0: init batch
                .next(internalComputeBatchStep)     // step 1: compute files that need to be backed up
                .next(backupToInternalStorageStep)  // step 2: perform backup
                .build();
    }

    /**
     * A {@link Step} that computes the backup batch using local information
     * and stores it into the context
     */
    @Bean
    @JobScope
    protected Step internalComputeBatchStep(
            BackupJobContext jobContext,
            FilesystemItemReader filesystemItemReader,
            ItemProcessor<LocalFileInfo, LocalFileInfo> internalCountingAndFilteringItemProcessor,
            ComputeBatchStepExecutionListener computeBatchStepExecutionListener) {
        return stepBuilderFactory
                .get("internalComputeBatchStep")
                .<LocalFileInfo, LocalFileInfo> chunk(10)
                // read files from local filesystem
                .reader(filesystemItemReader)
                // filter out files that don't need backup.
                .processor(internalCountingAndFilteringItemProcessor)
                // store them in the context
                .writer(new FileSetItemWriter(jobContext.getBackupBatch()))
                // update BackupSet with stats
                .listener(computeBatchStepExecutionListener)
                .build();
    }

    /**
     * A job-scoped {@link ItemProcessor} that filters out {@link LocalFileInfo} items
     * corresponding to a file that isn't different from the latest stored version, base on MD5 hashes.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor internalFilteringPathItemProcessor(
            @Qualifier("springMD5Calculator") InputStreamHashCalculator hashCalculator,
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        val storedFileVersionInfoProvider = getStoredFileVersionInfoProvider(backupSetId);
        val filteringStrategy = new HashFilteringStrategy(storedFileVersionInfoProvider, hashCalculator);
        return new FilteringPathItemProcessor(storedFileVersionInfoProvider, filteringStrategy);
    }

    /**
     * A job-scoped composite {@link ItemProcessor} that does the following:
     * - increment this job's total file and byte count using {@link #countingProcessor}
     * - filter out unchanged files using {@link #internalFilteringPathItemProcessor}
     */
    @Bean
    @JobScope
    protected ItemProcessor<LocalFileInfo, LocalFileInfo> internalCountingAndFilteringItemProcessor(
            ItemProcessor<LocalFileInfo, LocalFileInfo> countingProcessor,
            FilteringPathItemProcessor internalFilteringPathItemProcessor) {
        return new CompositeItemProcessor<LocalFileInfo, LocalFileInfo>() {
            {
                setDelegates(Arrays.asList(
                        countingProcessor,
                        internalFilteringPathItemProcessor
                ));
            }
        };
    }

    /**
     * Provides a {@link Step} that writes items from the current batch by storing them to the internal storage
     */
    @Bean
    @JobScope
    protected Step backupToInternalStorageStep(
            BackupJobContext jobContext,
            InternalStorageItemWriter internalStorageWriter,
            FileBackupListener fileBackupListener
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
                // monitor progress
                .listener(fileBackupListener)
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
        val storageService = storageServiceFactory.getStorageService(backupSetId);
        return new InternalStorageItemWriter(storageService, properties.getFileThrottleDelay());
    }

    protected LatestStoredRevisionProvider getStoredFileVersionInfoProvider(String backupSetId) {
        val storageService = storageServiceFactory.getStorageService(backupSetId);
        return new StorageServiceLatestStoredRevisionProviderAdapter(storageService);
    }


    @Bean
    @JobScope
    protected Step internalInitBatchStep(
            BackupJobContext jobContext
    ) {
        val storedFileVersionInfoProvider = getStoredFileVersionInfoProvider(jobContext.getBackupSetId());
        return stepBuilderFactory.get("internalInitBatchStep")
                .tasklet(new InitBatchTasklet(storedFileVersionInfoProvider, jobContext))
                .build();
    }

}
