package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.batch.BackupJobExecutionListener;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Job implementation for a backup job that process a source of type {@link org.ogerardin.b2b.domain.FilesystemSource}
 * and backup to a local destination (i.e. a {@link StorageService}).
 */
@Configuration
public class FilesystemToInternalBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

    @Autowired
    B2BProperties properties;

    @Autowired
    @Qualifier("gridFsStorageServiceFactory")
    private StorageServiceFactory storageServiceFactory;

    public FilesystemToInternalBackupJobConfiguration() {
        addStaticParameter("target.type", LocalTarget.class.getName());
    }

    @Bean
    protected Job filesystemToLocalBackupJob(
            Step backupToInternalStorageStep,
            Step listFilesStep,
            BackupJobExecutionListener jobListener) {
        return jobBuilderFactory
                .get(FilesystemToInternalBackupJobConfiguration.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(listFilesStep) //step 1: list files and put them in the job context
                .next(backupToInternalStorageStep) //step 2: process each file
                .build();
    }

    @Bean
    protected Step backupToInternalStorageStep(
            ItemReader<Path> contextItemReader,
            PathFilteringItemProcessor pathFilteringItemProcessor,
            PathItemProcessListener itemProcessListener,
            ItemWriter<Path> internalStorageWriter) {
        return stepBuilderFactory
                .get("processLocalFiles")
                .<Path, Path> chunk(10)
                .reader(contextItemReader)
                .processor(pathFilteringItemProcessor)
                .writer(internalStorageWriter)
                .listener(itemProcessListener)
                .build();
    }

    @Bean
    @JobScope
    protected PathFilteringItemProcessor internalStorageItemProcessor(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        // we use the backupSetId as storage service name; for GridFsStorageService this translates to the
        // bucket name used by GridFS so that all the files backed up as part of a backupSet are stored in a
        // distinct bucket
        // TODO we should implement a maintenance job to delete buckets for which there is no backupSet
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new PathFilteringItemProcessor(storageService);
    }

    @Bean
    @JobScope
    protected InternalStorageItemWriter internalStorageItemWriter(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    )  {
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new InternalStorageItemWriter(storageService, properties.getFileThrottleDelay());
    }

}
