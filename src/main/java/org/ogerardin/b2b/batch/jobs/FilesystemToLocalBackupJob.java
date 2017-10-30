package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
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
public class FilesystemToLocalBackupJob extends FilesystemSourceBackupJob {

    @Autowired
    B2BProperties properties;

    @Autowired
    @Qualifier("gridFsStorageServiceFactory")
    private StorageServiceFactory storageServiceFactory;

    public FilesystemToLocalBackupJob() {
        addStaticParameter("target.type", LocalTarget.class.getName());
    }

    @Bean("localToLocalJob")
    protected Job job(Step localToLocalStep, Step listStep, JobExecutionListener jobListener) {
        return jobBuilderFactory.get(FilesystemSourceBackupJob.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(listStep) //step 1: lists files and puts them in the job context
                .next(localToLocalStep) //step 2: process each file
                .build();
    }

    @Bean("localToLocalStep")
    protected Step step1(
            ItemReader<Path> localToLocalItemReader,
            ItemProcessor<Path, PathItemResult> localToLocalItemProcessor,
            PathItemProcessListener itemProcessListener) {
        return stepBuilderFactory
                .get("processLocalFiles")
                .<Path, PathItemResult> chunk(10)
                .reader(localToLocalItemReader)
                .processor(localToLocalItemProcessor)
//                .writer(newWriter())
                .listener(itemProcessListener)
                .build();
    }

    @Bean("listStep")
    @JobScope
    protected Step step0(
            Tasklet listFilesTasklet,
            ListFilesTaskletExecutionListener listFilesTaskletListener) {
        return stepBuilderFactory
                .get("listFiles")
                .tasklet(listFilesTasklet)
                .listener(listFilesTaskletListener)
                .build();
    }

    @Bean
    @JobScope
    protected ListFilesTasklet listFilesTasklet(
            @Value("#{jobParameters['source.root']}") String sourceRootParam,
            BackupJobContext backupJobContext
    ) {
        return new ListFilesTasklet(sourceRootParam, backupJobContext);
    }

    @Bean(name = "localToLocalItemProcessor")
    @JobScope
    protected ItemProcessor<Path, PathItemResult> itemProcessor(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        // we use the backupSetId as storage service name; for GridFsStorageService this translates to the
        // bucket name used by GridFS so that all the files backed up as part of a backupSet are stored in a
        // distinct bucket
        // TODO we should implement a maintenance job to delete buckets for which there is no backupSet
        StorageService storageService = storageServiceFactory.getStorageService(backupSetId);
        return new LocalStorageItemProcessor(storageService, properties.getThrottleDelay());
    }

    @Bean
    @JobScope
    protected ItemReader<Path> localToLocalItemReader(
            BackupJobContext backupJobContext
    ) {
        return new IteratorItemReader<>(backupJobContext.getAllFiles());
    }

    /**
     * Provides a job-scoped context that contains mostly the list of files to back up. We do not use
     * {@link org.springframework.batch.core.scope.context.JobContext} because it has limitations on size (and we
     * don't need to persist it anyway)
     */
    @Bean
    @JobScope
    protected BackupJobContext jobContext(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        return new BackupJobContext(backupSetId);
    }

}
