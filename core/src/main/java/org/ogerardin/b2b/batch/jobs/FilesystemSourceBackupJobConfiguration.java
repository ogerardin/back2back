package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.ogerardin.b2b.batch.jobs.support.HashFilteringStrategy;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.domain.mongorepository.FileBackupStatusInfoRepository;
import org.ogerardin.b2b.hash.HashProvider;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstract superclass for jobs that accept a source of type {@link FilesystemSource}
 */
public abstract class FilesystemSourceBackupJobConfiguration extends BackupJobConfiguration {

    @Autowired
    private MongoOperations mongoOperations;

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

    /**
     * A job-scoped {@link ItemProcessor} that filters out {@link LocalFileInfo} items
     * corresponding to a file that isn't different from the latest stored version, base on file hashes.
     */
    @Bean
    @JobScope
    protected FilteringItemProcessor filteringProcessor(
            @Qualifier("javaMD5Calculator") HashProvider hashProvider,
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider
    ) {
        val filteringStrategy = new HashFilteringStrategy(fileBackupStatusInfoProvider, hashProvider);
        return new FilteringItemProcessor(filteringStrategy);
    }

    @Bean
    @JobScope
    protected FileBackupStatusInfoProvider fileBackupStatusInfoProvider(
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        // The repository needs to be specific to this BackupSet, so we use a collection name derived from the backup set ID.
        String collectionName = backupSetId + ".hash";

        // to customize collection name for an entity we need to build a taylored MappingMongoEntityInformation
        val mappingContext = mongoOperations.getConverter().getMappingContext();
        //noinspection unchecked
        val entity = (MongoPersistentEntity<FileBackupStatusInfo>) mappingContext.getPersistentEntity(FileBackupStatusInfo.class);
        val entityInformation = new MappingMongoEntityInformation<FileBackupStatusInfo, String>(entity, collectionName);

        return new FileBackupStatusInfoRepository(entityInformation, mongoOperations);
    }
}
