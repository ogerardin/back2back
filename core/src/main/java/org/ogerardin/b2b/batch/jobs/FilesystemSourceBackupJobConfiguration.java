package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.SetItemWriter;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

/**
 * Abstract superclass for jobs that accept a source of type {@link FilesystemSource}
 */
public abstract class FilesystemSourceBackupJobConfiguration extends BackupJobConfiguration {


    public FilesystemSourceBackupJobConfiguration() {
        addStaticParameter("source.type", FilesystemSource.class.getName());
        addMandatoryParameter("source.root");
    }

    /** Provides a {@link Tasklet} that populates the current job's {@link BackupJobContext#allFiles} */
    @Bean
    @JobScope
    protected ListFilesTasklet listFilesTasklet(
            @Value("#{jobParameters['source.root']}") String sourceRootParam,
            BackupJobContext backupJobContext
    ) {
        return new ListFilesTasklet(sourceRootParam, backupJobContext);
    }


    /** Provides a {@link ItemReader} that supplies {@link FileInfo} items from the current job's
     * {@link BackupJobContext#allFiles} */
    @Bean
    @JobScope
    protected IteratorItemReader<FileInfo> allFilesItemReader(
            BackupJobContext backupJobContext
    ) {
        return new IteratorItemReader<>(backupJobContext.getAllFiles());
    }


    /** Provides a {@link ItemReader} that supplies {@link FileInfo} items from the current job's
     * {@link BackupJobContext#toDoFiles} */
    @Bean
    @JobScope
    protected IteratorItemReader<FileInfo> changedFilesItemReader(
            BackupJobContext backupJobContext
    ) {
        return new IteratorItemReader<>(backupJobContext.getToDoFiles());
    }


    /** Provides an {@link ItemWriter} that stores {@link Path} items in the current job's
     * {@link BackupJobContext#toDoFiles} */
    @Bean
    @JobScope
    protected SetItemWriter<FileInfo> changedFilesItemWriter(
            BackupJobContext backupJobContext
    ) {
        return new SetItemWriter<>(backupJobContext.getToDoFiles());
    }


    /**
     * Provides a {@link Step} that implements the first step of a backup job: populate the current job's
     * {@link BackupJobContext} with the list of all files from the backup source
     */
    @Bean
    @JobScope
    protected Step listFilesStep(
            Tasklet listFilesTasklet,
            ListFilesTaskletExecutionListener listFilesTaskletListener) {
        return stepBuilderFactory
                .get("listFilesStep")
                .tasklet(listFilesTasklet)
                .listener(listFilesTaskletListener)
                .build();
    }

    /**
     * Provides a {@link Step} that implements the second step of a backup job: populate the current job's
     * {@link BackupJobContext} with the list of CHANGED files
     */
    @Bean
    @JobScope
    protected Step filterFilesStep(
            ItemReader<FileInfo> allFilesItemReader,
            FilteringPathItemProcessor pathFilteringItemProcessor,
            ItemWriter<FileInfo> changedFilesItemWriter) {
        return stepBuilderFactory
                .get("filterFilesStep")
                .<FileInfo, FileInfo> chunk(10)
                .reader(allFilesItemReader)
                .processor(pathFilteringItemProcessor)
                .writer(changedFilesItemWriter)
                .build();
    }


    /**
     * Provides a job-scoped context that contains contextual data for the current job, including the list of files
     * to backup.
     * We do not use {@link org.springframework.batch.core.scope.context.JobContext} because it has limitations on
     * size  (and we don't need to persist it anyway)
     */
    @Bean
    @JobScope
    protected BackupJobContext jobContext(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        return new BackupJobContext(backupSetId);
    }

}
