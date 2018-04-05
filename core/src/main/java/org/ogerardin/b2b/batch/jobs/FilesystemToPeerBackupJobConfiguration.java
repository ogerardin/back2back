package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.PeerTarget;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.mongorepository.RemoteFileVersionInfoRepository;
import org.ogerardin.b2b.files.md5.StreamingMd5Calculator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
            Step initBatchRemoteStep,
            Step computeBatchStep,
            Step backupToPeerStep,
            BackupJobExecutionListener jobListener
    ) {
        return jobBuilderFactory
                .get("filesystemToPeerBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(initBatchRemoteStep)        // step 0: init batch
                .next(computeBatchStep)            // step 1: compute files that need to be backed up
                .next(backupToPeerStep)            // step 2: perform backup
                .build();
    }

    @Bean
    @JobScope
    protected Step initBatchRemoteStep(
            RemoteFileVersionInfoRepository peerFileVersionRepository
    ) {
        return stepBuilderFactory.get("initBatchRemoteStep")
                .tasklet(new InitBatchTasklet(peerFileVersionRepository))
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
            @Qualifier("peerFileVersionRepository") RemoteFileVersionInfoRepository peerFileVersionInfoRepository) throws MalformedURLException {
        if (targetPort == null) {
            targetPort = properties.getDefaultPeerPort();
        }
        return new PeerItemWriter(peerFileVersionInfoRepository, targetHostname, targetPort);
    }

    /**
     * Provides a job-scoped {@link ItemProcessor} that filters out {@link Path} items
     * corresponding to a file that isn't different from the latest stored version.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor peerFilteringPathItemProcessor(
            @Qualifier("springMD5Calculator") StreamingMd5Calculator md5Calculator,
            RemoteFileVersionInfoRepository peerFileVersionRepository
    ) {
        Md5FilteringStrategy filteringStrategy = new Md5FilteringStrategy(peerFileVersionRepository, md5Calculator);
        return new FilteringPathItemProcessor(peerFileVersionRepository, filteringStrategy);
    }

    @Bean
    @JobScope
    protected RemoteFileVersionInfoRepository peerFileVersionRepository(
            @Value("#{jobParameters['backupset.id']}"
            ) String backupSetId) {

        // The RemoteFileVersionInfoRepository used by the PeerItemWriter needs to be specific to this BackupSet,
        // so we need to instantiate one with a BackupSet-specific collection name
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
