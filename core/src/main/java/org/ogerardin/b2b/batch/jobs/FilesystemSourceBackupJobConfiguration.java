package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.files.md5.InputStreamMD5Calculator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean
    @JobScope
    protected Step initBatchStep(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        val storedFileVersionInfoProvider = storedFileVersionInfoProvider(backupSetId);
        return stepBuilderFactory.get("initBatchStep")
                .tasklet(new InitBatchTasklet(storedFileVersionInfoProvider))
                .build();
    }



    /**
     * Provides a job-scoped context that contains contextual data for the current job, including the list of files
     * to backup.
     * We do not use {@link JobContext} because it has limitations on
     * size (and we don't want to persist it anyway)
     */
    @Bean
    @JobScope
    protected BackupJobContext jobContext(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        return new BackupJobContext(backupSetId);
    }


    @Bean
    @JobScope
    protected FilesystemItemReader filesystemItemReader(
            @Value("#{jobParameters['source.roots']}") String sourceRootsParam
    ) throws IOException {
        List<Path> roots = OBJECT_MAPPER.readValue(sourceRootsParam, new TypeReference<List<Path>>() {});
        return new FilesystemItemReader(roots);
    }

    /**
     * Provides a job-scoped {@link ItemProcessor} that filters out {@link Path} items
     * corresponding to a file that isn't different from the latest stored version, base on MD5 hashes.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor filteringPathItemProcessor(
            @Qualifier("springMD5Calculator") InputStreamMD5Calculator md5Calculator,
            @Qualifier("storedFileVersionInfoProvider") StoredFileVersionInfoProvider storedFileVersionInfoProvider) {
        Md5FilteringStrategy filteringStrategy = new Md5FilteringStrategy(storedFileVersionInfoProvider, md5Calculator);
        return new FilteringPathItemProcessor(storedFileVersionInfoProvider, filteringStrategy);
    }

    @Bean
    @JobScope
    abstract StoredFileVersionInfoProvider storedFileVersionInfoProvider(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    );

}
