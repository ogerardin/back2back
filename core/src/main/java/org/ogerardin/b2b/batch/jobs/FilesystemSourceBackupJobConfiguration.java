package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.FilesystemSource;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
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

    @Bean
    @JobScope
    protected ListFilesTasklet listFilesTasklet(
            @Value("#{jobParameters['source.root']}") String sourceRootParam,
            BackupJobContext backupJobContext
    ) {
        return new ListFilesTasklet(sourceRootParam, backupJobContext);
    }

    @Bean
    @JobScope
    protected ItemReader<Path> contextItemReader(
            BackupJobContext backupJobContext
    ) {
        return new IteratorItemReader<>(backupJobContext.getAllFiles());
    }

    @Bean
    @JobScope
    protected Step listFilesStep(
            Tasklet listFilesTasklet,
            ListFilesTaskletExecutionListener listFilesTaskletListener) {
        return stepBuilderFactory
                .get("listFiles")
                .tasklet(listFilesTasklet)
                .listener(listFilesTaskletListener)
                .build();
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
