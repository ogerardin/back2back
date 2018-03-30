package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.ogerardin.b2b.files.md5.MD5Calculator;
import org.ogerardin.b2b.files.md5.StreamingMd5Calculator;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
            Step backupToInternalStorageStep,
            BackupJobExecutionListener jobListener) {
        return jobBuilderFactory
                .get("filesystemToInternalBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(backupToInternalStorageStep)
                .build();
    }

    @Bean
    @JobScope
    protected FilesystemItemReader filesystemItemReader(
            @Value("#{jobParameters['source.roots']}") String sourceRootsParam,
            BackupJobContext backupJobContext
    ) throws IOException {
        List<Path> roots = OBJECT_MAPPER.readValue(sourceRootsParam, new TypeReference<List<Path>>() {});
        return new FilesystemItemReader(roots, backupJobContext);
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
     * We use a {@link MD5Calculator} for determining if the file's hash has changed.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor filteringPathItemProcessor(
            @Value("#{jobParameters['backupset.id']}") String backupSetId,
            @Qualifier("springMD5Calculator") StreamingMd5Calculator md5Calculator
    ) {
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new FilteringPathItemProcessor(StoredFileVersionInfoProvider.of(storageService), md5Calculator);
    }

    /**
     * Provides a {@link Step} that:
     * -reads items by walking the filesystem from the configured roots
     * -processes item by filtering out unchanged files
     * -writes items by storing them to the internal storage
     */
    @Bean
    @JobScope
    protected Step backupToInternalStorageStep(
            FilesystemItemReader filesystemItemReader,
            FilteringPathItemProcessor filteringPathItemProcessor,
            InternalStorageItemWriter internalStorageWriter
    ) {
        return stepBuilderFactory
                .get("backupToInternalStorageStep")
                .<LocalFileInfo, LocalFileInfo> chunk(10)
                .reader(filesystemItemReader)
                .processor(filteringPathItemProcessor)
                .writer(internalStorageWriter)
                .build();
    }



}
