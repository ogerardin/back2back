package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.FileSetItemWriter;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.PeerTarget;
import org.ogerardin.b2b.domain.entity.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.mongorepository.RemoteFileVersionInfoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;

import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * Job configuration for a backup job that processes a source of type {@link FilesystemSource}
 * and backs up to a network peer.
 */
@Configuration
public class FilesystemToPeerBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

    @Autowired
    private MongoOperations mongoOperations;

    public FilesystemToPeerBackupJobConfiguration() {
        addStaticParameter("target.type", PeerTarget.class.getName());
        addMandatoryParameter("target.hostname");
//        addMandatoryParameter("target.port");
    }

    @Bean
    protected Job filesystemToPeerBackupJob(
            Step initBatchStep,
            Step peerComputeBatchStep,
            Step backupToPeerStep,
            BackupJobExecutionListener jobListener
    ) {
        return jobBuilderFactory
                .get("filesystemToPeerBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(initBatchStep)        // step 0: init batch
                .next(peerComputeBatchStep)        // step 1: compute files that need to be backed up
                .next(backupToPeerStep)            // step 2: perform backup
                .build();
    }

    /**
     * Provides a {@link Step} that computes the backup batch using peer information
     * and stores it into the context
     */
    @Bean
    @JobScope
    protected Step peerComputeBatchStep(
            BackupJobContext jobContext,
            FilesystemItemReader filesystemItemReader,
            FilteringPathItemProcessor peerFilteringPathItemProcessor,
            ComputeBatchStepExecutionListener computeBatchStepExecutionListener) {
        return stepBuilderFactory
                .get("computeBatchStep")
                .<LocalFileInfo, LocalFileInfo> chunk(10)
                // read files from local filesystem
                .reader(filesystemItemReader)
                // filter out files that don't need backup
                .processor(peerFilteringPathItemProcessor)
                // store them in the context
                .writer(new FileSetItemWriter(jobContext.getBackupBatch()))
                // update BackupSet with stats
                .listener(computeBatchStepExecutionListener)
                .build();
    }

    /**
     * Provides a {@link Step} that writes items from the current batch by storing them to the remote peer
     */
    @Bean
    @JobScope
    protected Step backupToPeerStep(
            BackupJobContext jobContext,
            PeerItemWriter peerWriter
    ) {
        return stepBuilderFactory
                .get("backupToPeerStep")
                .<LocalFileInfo, LocalFileInfo>chunk(1) // handle 1 file at a time
                // read files from job context
                .reader(new IteratorItemReader<>(jobContext.getBackupBatch().getFiles()))
                // no processing
                .processor(new PassThroughItemProcessor<>())
                // store them to the remote peer
                .writer(peerWriter)
                .build();
    }



    /**
     * Provides a job-scoped {@link ItemWriter} that stores {@link Path} items to
     * the remote peer
     */
    @Bean
    @JobScope
    protected PeerItemWriter peerItemWriter(
            @Value("#{jobParameters['target.hostname']}") String targetHostname,
            @Value("#{jobParameters['target.port']}") Integer targetPort,
            StoredFileVersionInfoProvider storedFileVersionInfoProvider) throws MalformedURLException {
        if (targetPort == null) {
            targetPort = properties.getDefaultPeerPort();
        }
        return new PeerItemWriter(storedFileVersionInfoProvider, targetHostname, targetPort);
    }

    protected StoredFileVersionInfoProvider storedFileVersionInfoProvider(String backupSetId) {
        // The RemoteFileVersionInfoRepository used by the PeerItemWriter needs to be specific to this BackupSet,
        // so use a BackupSet-derived collection name
        String collectionName = backupSetId + ".peer";

        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext =
                mongoOperations.getConverter().getMappingContext();

        MongoPersistentEntity<?> entity = mappingContext.getPersistentEntity(StoredFileVersionInfo.class);
        //noinspection unchecked
        MappingMongoEntityInformation<StoredFileVersionInfo, String> entityInformation = new MappingMongoEntityInformation<>(
                (MongoPersistentEntity<StoredFileVersionInfo>) entity, collectionName);

        return new RemoteFileVersionInfoRepository(entityInformation, mongoOperations);
    }




}
