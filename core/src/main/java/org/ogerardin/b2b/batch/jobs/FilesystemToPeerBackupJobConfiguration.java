package org.ogerardin.b2b.batch.jobs;

import lombok.val;
import org.ogerardin.b2b.batch.FileSetItemWriter;
import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.LatestStoredRevision;
import org.ogerardin.b2b.domain.entity.PeerTarget;
import org.ogerardin.b2b.domain.mongorepository.LatestStoredRevisionRepository;
import org.ogerardin.b2b.files.md5.InputStreamMD5Calculator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.repository.support.MappingMongoEntityInformation;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;

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
            Step peerInitBatchStep,
            Step peerComputeBatchStep,
            Step backupToPeerStep,
            BackupJobExecutionListener jobListener
    ) {
        return jobBuilderFactory
                .get("filesystemToPeerBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(peerInitBatchStep)        // step 0: init batch
                .next(peerComputeBatchStep)  // step 1: compute files that need to be backed up
                .next(backupToPeerStep)      // step 2: perform backup
                .build();
    }

    /**
     * A {@link Step} that computes the backup batch using peer information
     * and stores it into the context
     */
    @Bean
    @JobScope
    protected Step peerComputeBatchStep(
            BackupJobContext jobContext,
            FilesystemItemReader filesystemItemReader,
            ItemProcessor<LocalFileInfo, LocalFileInfo> peerCountingAndFilteringItemProcessor,
            ComputeBatchStepExecutionListener computeBatchStepExecutionListener) {
        return stepBuilderFactory
                .get("peerComputeBatchStep")
                .<LocalFileInfo, LocalFileInfo> chunk(10)
                // read files from local filesystem
                .reader(filesystemItemReader)
                // filter out files that don't need backup
                .processor(peerCountingAndFilteringItemProcessor)
                // store them in the context
                .writer(new FileSetItemWriter(jobContext.getBackupBatch()))
                // update BackupSet with stats
                .listener(computeBatchStepExecutionListener)
                .build();
    }

    /**
     * A job-scoped {@link ItemProcessor} that filters out {@link LocalFileInfo} items
     * corresponding to a file that isn't different from the latest stored version, base on MD5 hashes.
     */
    @Bean
    @JobScope
    protected FilteringPathItemProcessor peerFilteringPathItemProcessor(
            @Qualifier("springMD5Calculator") InputStreamMD5Calculator md5Calculator,
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        val storedFileVersionInfoProvider = getStoredFileVersionInfoProvider(backupSetId);
        val filteringStrategy = new Md5FilteringStrategy(storedFileVersionInfoProvider, md5Calculator);
        return new FilteringPathItemProcessor(storedFileVersionInfoProvider, filteringStrategy);
    }

    /**
     * A job-scoped composite {@link ItemProcessor} that does the following:
     * - increment this job's total file and byte count using {@link #countingProcessor}
     * - filter out unchanged files using {@link #peerFilteringPathItemProcessor}
     */
    @Bean
    @JobScope
    protected ItemProcessor<LocalFileInfo, LocalFileInfo> peerCountingAndFilteringItemProcessor(
            ItemProcessor<LocalFileInfo, LocalFileInfo> countingProcessor,
            FilteringPathItemProcessor peerFilteringPathItemProcessor) {
        return new CompositeItemProcessor<LocalFileInfo, LocalFileInfo>() {
            {
                setDelegates(Arrays.asList(
                        countingProcessor,
                        peerFilteringPathItemProcessor
                ));
            }
        };
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
            @Value("#{jobParameters['backupset.id']}") String backupSetId) throws MalformedURLException {
        if (targetPort == null) {
            targetPort = properties.getDefaultPeerPort();
        }
        val storedFileVersionInfoProvider = getStoredFileVersionInfoProvider(backupSetId);
        return new PeerItemWriter(storedFileVersionInfoProvider, targetHostname, targetPort);
    }

    protected LatestStoredRevisionProvider getStoredFileVersionInfoProvider(String backupSetId) {
        // The LatestStoredRevisionRepository used by the PeerItemWriter needs to be specific to this BackupSet,
        // so we use a collection name derived from the backup set ID.
        String collectionName = backupSetId + ".peer";

        // to customize collection name we need to build a taylored MappingMongoEntityInformation
        val mappingContext = mongoOperations.getConverter().getMappingContext();
        //noinspection unchecked
        val entity = (MongoPersistentEntity<LatestStoredRevision>) mappingContext.getPersistentEntity(LatestStoredRevision.class);
        val entityInformation = new MappingMongoEntityInformation<LatestStoredRevision, String>(entity, collectionName);

        return new LatestStoredRevisionRepository(entityInformation, mongoOperations);
    }


    @Bean
    @JobScope
    protected Step peerInitBatchStep(
            BackupJobContext jobContext
    ) {
        val storedFileVersionInfoProvider = getStoredFileVersionInfoProvider(jobContext.getBackupSetId());
        return stepBuilderFactory.get("peerInitBatchStep")
                .tasklet(new InitBatchTasklet(storedFileVersionInfoProvider, jobContext))
                .build();
    }
}
