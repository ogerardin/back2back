package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
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

    @Autowired
    private B2BProperties properties;

    public FilesystemToInternalBackupJobConfiguration() {
        addStaticParameter("target.type", LocalTarget.class.getName());
    }

    /**
     * Provides a job that performs the backup of a filesystem source into the internal storage.
     */
    @Bean
    protected Job filesystemToInternalBackupJob(
            Step listFilesStep,
            Step filterFilesStep,
            Step backupToInternalStorageStep,
            Step computeBatchSizeStep,
            BackupJobExecutionListener jobListener) {
        return jobBuilderFactory
                .get("filesystemToInternalBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(listFilesStep)               //step 1: list files and put them in the job context
                .next(filterFilesStep)              //step 2: filter unchanged files
                .next(computeBatchSizeStep)         //step 3: compute backup batch size
                .next(backupToInternalStorageStep)  //step 4: save changed files
                .build();
    }

    /**
     * Provides a {@link Step} that performs backup of the files taken from the current job's
     * {@link BackupJobContext#toDoFiles} into the internal storage.
     */
    @Bean
    @JobScope
    protected Step backupToInternalStorageStep(
            ItemReader<FileInfo> changedFilesItemReader,
            InternalStorageItemWriter internalStorageWriter,
            PathItemWriteListener itemWriteListener) {
        return stepBuilderFactory
                .get("backupToInternalStorageStep")
                .<FileInfo, FileInfo> chunk(1)  // invoke writer 1 file at a time
                .reader(changedFilesItemReader)
                .processor(new PassThroughItemProcessor<>()) // no processing
                .writer(internalStorageWriter)
                .listener(itemWriteListener)
                .build();
    }


    /**
     * Provides a job-scoped {@link org.springframework.batch.item.ItemWriter} that stores {@link Path} items into
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
     * We use a {@link MD5Calculator} for determining if the file's hash has changed.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor filteringPathItemProcessor(
            @Value("#{jobParameters['backupset.id']}") String backupSetId,
            @Qualifier("springMD5Calculator") MD5Calculator md5Calculator
    ) {
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new FilteringPathItemProcessor(storageService, md5Calculator);
    }
}
