package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.B2BProperties;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.PeerTarget;
import org.ogerardin.b2b.domain.mongorepository.PeerFileVersionInfoRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
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

/**
 * Job configuration for a backup job that processes a source of type {@link org.ogerardin.b2b.domain.FilesystemSource}
 * and backs up to a network peer.
 */
@Configuration
public class FilesystemToPeerBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

    @Autowired
    B2BProperties properties;

    @Autowired
    private MongoOperations mongoOperations;

    public FilesystemToPeerBackupJobConfiguration() {
        addStaticParameter("target.type", PeerTarget.class.getName());
        addMandatoryParameter("target.hostname");
//        addMandatoryParameter("target.port");
    }

    @Bean
    protected Job filesystemToPeerBackupJob(
            Step listFilesStep,
            Step backupToPeerStep,
            BackupJobExecutionListener jobListener
    ) {
        return jobBuilderFactory
                .get(FilesystemToPeerBackupJobConfiguration.class.getSimpleName())
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(listFilesStep)       //step 1: list files and put them in the job context
                //.next(filterFilesStep)      //step 2: filter unchanged files
                //.next(computeBatchSizeStep)     //step 3: compute backup batch size
                .next(backupToPeerStep)     //step 4: backup
                .build();
    }

    @Bean
    protected Step backupToPeerStep(
            ItemReader<LocalFileInfo> allFilesItemReader,
            PeerItemWriter peerWriter
    )
    {
        return stepBuilderFactory.get("backupToPeerStep")
                .<LocalFileInfo, LocalFileInfo>chunk(1) // handles 1 file at a time
                .reader(allFilesItemReader)
                .processor(new PassThroughItemProcessor<>())
                .writer(peerWriter)
                .build();
    }

    @Bean
    @JobScope
    protected PeerItemWriter peerItemWriter(
            @Value("#{jobParameters['target.hostname']}") String targetHostname,
            @Value("#{jobParameters['target.port']}") Integer targetPort,
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        if (targetPort == null) {
            targetPort = properties.getDefaultPeerPort();
        }

        // The PeerFileVersionInfoRepository used by the PeerItemWriter needs to be specific to this BackupSet,
        // so we need to instantiate one with a BackupSet-specific collection name
        String collectionName = backupSetId + ".peer";
        PeerFileVersionInfoRepository peerFileVersionInfoRepository = getPeerFileVersionRepository(collectionName);

        return new PeerItemWriter(peerFileVersionInfoRepository, targetHostname, targetPort);
    }

    private PeerFileVersionInfoRepository getPeerFileVersionRepository(String collectionName) {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext =
                mongoOperations.getConverter().getMappingContext();

        MongoPersistentEntity<?> entity = mappingContext.getPersistentEntity(StoredFileVersionInfo.class);

        MappingMongoEntityInformation<StoredFileVersionInfo, String> entityInformation = new MappingMongoEntityInformation<>(
                (MongoPersistentEntity<StoredFileVersionInfo>) entity, collectionName);

        return new PeerFileVersionInfoRepository(entityInformation, mongoOperations);
    }


}
