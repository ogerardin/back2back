package org.ogerardin.b2b.batch.jobs;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.val;
import org.ogerardin.b2b.batch.jobs.listeners.ComputeBatchStepItemWriteListener;
import org.ogerardin.b2b.batch.jobs.listeners.InitTaskletExecutionListener;
import org.ogerardin.b2b.batch.jobs.support.HashFilteringStrategy;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.mongorepository.FileBackupStatusInfoRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
     * A {@link Step} that computes the backup batch using local information
     */
    @Bean
    @JobScope
    protected Step computeBatchStep(
            FilesystemItemReader filesystemItemReader,
            BackupStatusUpdater backupStatusUpdater,
            HashingItemProcessor hashingItemProcessor,
            StatusComputingItemProcessor statusComputingItemProcessor,
            BackupFlagComputingItemProcessor backupFlagComputingItemProcessor,
            ComputeBatchStepItemWriteListener computeBatchStepItemWriteListener) {
        return stepBuilderFactory
                .get("computeBatchStep")
                .<LocalFileInfo, FileBackupStatusInfo>chunk(properties.getListFilesChunkSize())
                // read files from local filesystem
                .reader(filesystemItemReader)
                // compute FileBackupStatusInfo
                .processor(new CompositeItemProcessor<LocalFileInfo, FileBackupStatusInfo>() {
                    {
                        setDelegates(Arrays.asList(
                                hashingItemProcessor,               // compute hash of current file
                                statusComputingItemProcessor,       // load or create corresponding FileBackupStatusInfo
                                backupFlagComputingItemProcessor    // test if backup required

                        ));
                    }
                })
                // saved the FileBackupStatusInfo
                .writer(backupStatusUpdater)
                // update BackupSet with stats
                .listener(computeBatchStepItemWriteListener)
                .build();
    }

    @Bean
    @JobScope
    protected StatusComputingItemProcessor statusComputingItemProcessor(
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider
    ) {
        return new StatusComputingItemProcessor(fileBackupStatusInfoProvider);
    }

    @Bean
    protected BackupFlagComputingItemProcessor backupFlagComputingItemProcessor(
            HashFilteringStrategy filteringStrategy
    ) {
        return new BackupFlagComputingItemProcessor(filteringStrategy);
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
        List<Path> roots = OBJECT_MAPPER.readValue(sourceRootsParam, new TypeReference<List<Path>>() {
        });
        return new FilesystemItemReader(roots);
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
        val entity = (MongoPersistentEntity<FileBackupStatusInfo>) mappingContext
                .getPersistentEntity(FileBackupStatusInfo.class);
        val entityInformation = new MappingMongoEntityInformation<FileBackupStatusInfo, String>(Objects
                .requireNonNull(entity), collectionName);

        return new FileBackupStatusInfoRepository(entityInformation, mongoOperations);
    }

    @Bean
    @JobScope
    protected Step initBatchStep(
            BackupJobContext jobContext,
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider,
            InitTaskletExecutionListener initTaskletExecutionListener) {
        return stepBuilderFactory
                .get("initBatchStep" + this.getClass().getSimpleName())
                .tasklet(new InitBatchTasklet(fileBackupStatusInfoProvider, jobContext))
                .listener(initTaskletExecutionListener)
                .build();
    }

    @Bean
    @JobScope
    protected Step finalizeBackupStep(
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider
    ) {
        return stepBuilderFactory
                .get("finalizeBackupStep")
                .tasklet(new FinalizeBackupTasklet(fileBackupStatusInfoProvider))
                .build();
    }

    @Bean
    @JobScope
    protected BackupStatusUpdater backupStatusUpdater(
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider
    ) {
        return new BackupStatusUpdater(fileBackupStatusInfoProvider);
    }

}
