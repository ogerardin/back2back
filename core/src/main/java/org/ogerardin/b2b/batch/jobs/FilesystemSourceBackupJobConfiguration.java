package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
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
     * A job-scoped object that contains contextual data for the current job, most notably the list of files
     * to backup.
     * We do not use {@link JobContext} because it has limitations on size (and we don't want to persist it anyway)
     */
    @Bean
    @JobScope
    protected BackupJobContext jobContext(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        return new BackupJobContext(backupSetId);
    }


    /**
     * An {@link ItemReader} that generates a list of {@link LocalFileInfo}, each corresponding to a local file
     * obtained by walking the source roots specified in this job's "source.roots" parameter.
     */
    @Bean
    @JobScope
    protected FilesystemItemReader filesystemItemReader(
            @Value("#{jobParameters['source.roots']}") String sourceRootsParam
    ) throws IOException {
        List<Path> roots = OBJECT_MAPPER.readValue(sourceRootsParam, new TypeReference<List<Path>>() {});
        return new FilesystemItemReader(roots);
    }

    /**
     * A job-scoped pass-through {@link ItemProcessor} that updates this job's total file and byte cound
     */
    @Bean
    @JobScope
    protected ItemProcessor<LocalFileInfo, LocalFileInfo> countingProcessor(BackupJobContext jobContext) {
        return item -> {
            long fileSize = item.getFileAttributes().size();
            jobContext.getTotalFileStats().addFile(fileSize);
            return item;
        };
    }

    abstract LatestStoredRevisionProvider getStoredFileVersionInfoProvider(String backupSetId);

}
