package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ogerardin.b2b.batch.FileSetItemWriter;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract superclass for jobs that accept a source of type {@link FilesystemSource}
 */
public abstract class FilesystemSourceBackupJobConfiguration extends BackupJobConfiguration {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public FilesystemSourceBackupJobConfiguration() {
        addStaticParameter("source.type", FilesystemSource.class.getName());
        addMandatoryParameter("source.roots");
    }

    /** Provides a {@link Tasklet} that populates the current job's {@link BackupJobContext#allFiles} */
    @Bean
    @JobScope
    protected ListFilesTasklet listFilesTasklet(
            @Value("#{jobParameters['source.roots']}") String sourceRootsParam,
            BackupJobContext backupJobContext
    ) throws IOException {
        List<Path> roots = OBJECT_MAPPER.readValue(sourceRootsParam, new TypeReference<List<Path>>() {});
        return new ListFilesTasklet(roots, backupJobContext);
    }


    /** Provides a {@link ItemReader} that supplies {@link LocalFileInfo} items from the current job's
     * {@link BackupJobContext#allFiles} */
    @Bean
    @JobScope
    protected IteratorItemReader<LocalFileInfo> allFilesItemReader(
            BackupJobContext backupJobContext
    ) {
        return new IteratorItemReader<>(backupJobContext.getAllFiles().getFiles());
    }


    /** Provides a {@link ItemReader} that supplies {@link LocalFileInfo} items from the current job's
     * {@link BackupJobContext#changedFiles} */
    @Bean
    @JobScope
    protected IteratorItemReader<LocalFileInfo> changedFilesItemReader(
            BackupJobContext backupJobContext
    ) {
        return new IteratorItemReader<>(backupJobContext.getChangedFiles().getFiles());
    }


    /** Provides an {@link ItemWriter} that stores {@link Path} items in the current job's
     * {@link BackupJobContext#changedFiles} */
    @Bean
    @JobScope
    protected FileSetItemWriter changedFilesItemWriter(
            BackupJobContext backupJobContext
    ) {
        return new FileSetItemWriter(backupJobContext.getChangedFiles());
    }


    /**
     * Provides a {@link Step} that implements the first step of a backup job: populate the current job's
     * {@link BackupJobContext} with the list of all files from the backup source
     */
    @Bean
    @JobScope
    protected Step listFilesStep(
            Tasklet listFilesTasklet,
            CollectingStepExecutionListener stepListener) {
        return stepBuilderFactory
                .get("listFilesStep")
                .tasklet(listFilesTasklet)
                .listener(stepListener)
                .build();
    }

    /**
     * Provides a job-scoped context that contains contextual data for the current job, including the list of files
     * to backup.
     * We do not use {@link org.springframework.batch.core.scope.context.JobContext} because it has limitations on
     * size (and we don't need to persist it anyway)
     */
    @Bean
    @JobScope
    protected BackupJobContext jobContext(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        return new BackupJobContext(backupSetId);
    }

}
