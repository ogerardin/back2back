package org.ogerardin.b2b.batch.jobs;

import lombok.val;
import org.ogerardin.b2b.batch.jobs.listeners.BackupJobExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.FileBackupListener;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.LocalTarget;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
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
     * A job that performs the backup of a filesystem source into the internal storage.
     */
    @Bean
    protected Job filesystemToInternalBackupJob(
            Step initBatchStep,
            Step computeBatchStep,
            Step internalBackupStep,
            BackupJobExecutionListener jobListener,
            Step finalizeBackupStep) {
        return jobBuilderFactory
                .get("filesystemToInternalBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(initBatchStep)       // step 0: init batch
                .next(computeBatchStep)     // step 1: compute hashes
                .next(internalBackupStep)   // step 2: perform backup
                .next(finalizeBackupStep)   // step 3: cleanup
                .build();
    }

    /**
     * Provides a {@link Step} that writes items from the current batch by storing them to the internal storage
     */
    @Bean
    @JobScope
    protected Step internalBackupStep(
            BackupStatusUpdater backupStatusUpdater,
            FileBackupListener fileBackupListener,
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider,
            BackupItemProcessor backupItemProcessor
    ) {
        return stepBuilderFactory
                .get("backupToInternalStorageStep")
                // handle 1 file at a time
                .<FileBackupStatusInfo, FileBackupStatusInfo> chunk(1)
                // read files from local backup status database
                .reader(fileBackupStatusInfoProvider.backupRequestedItemReader())
                // perform backup
                .processor(backupItemProcessor)
                // store backup status
                .writer(backupStatusUpdater)
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
    protected BackupItemProcessor backupItemProcessor(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    )  {
        val storageService = storageServiceFactory.getStorageService(backupSetId);

        return new BackupItemProcessor(
                storageService,
                properties.getFileThrottleDelay());
    }

}
