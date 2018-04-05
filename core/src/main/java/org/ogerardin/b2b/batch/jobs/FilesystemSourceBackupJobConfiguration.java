package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ogerardin.b2b.batch.FileSetItemWriter;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract superclass for jobs that accept a source of type {@link FilesystemSource}
 */
public abstract class FilesystemSourceBackupJobConfiguration extends BackupJobConfiguration {

    public FilesystemSourceBackupJobConfiguration() {
        addStaticParameter("source.type", FilesystemSource.class.getName());
        addMandatoryParameter("source.roots");
    }

    /**
     * Provides a job-scoped context that contains contextual data for the current job, including the list of files
     * to backup.
     * We do not use {@link org.springframework.batch.core.scope.context.JobContext} because it has limitations on
     * size (and we don't want to persist it anyway)
     */
    @Bean
    @JobScope
    protected BackupJobContext jobContext(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        return new BackupJobContext(backupSetId);
    }

    /**
     * Provides a {@link Step} that computes the backup batch and stores it into the context
     */
    @Bean
    @JobScope
    protected Step computeBatchStep(
            BackupJobContext jobContext,
            FilesystemItemReader filesystemItemReader,
            FilteringPathItemProcessor filteringPathItemProcessor,
            ComputeBatchStepExecutionListener computeBatchStepExecutionListener
    ) {
        return stepBuilderFactory
                .get("computeBatchStep")
                .<LocalFileInfo, LocalFileInfo> chunk(10)
                // read files from local filesystem
                .reader(filesystemItemReader)
                // filter out files that don't need backup
                .processor(filteringPathItemProcessor)
                // store them in the context
                .writer(new FileSetItemWriter(jobContext.getBackupBatch()))
                // update BackupSet with stats
                .listener(computeBatchStepExecutionListener)
                .build();
    }

    @Bean
    @JobScope
    protected FilesystemItemReader filesystemItemReader(
            @Value("#{jobParameters['source.roots']}") String sourceRootsParam
    ) throws IOException {
        List<Path> roots = OBJECT_MAPPER.readValue(sourceRootsParam, new TypeReference<List<Path>>() {});
        return new FilesystemItemReader(roots);
    }
}
