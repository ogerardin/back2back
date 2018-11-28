package org.ogerardin.b2b.batch.jobs;

import lombok.val;
import org.ogerardin.b2b.batch.jobs.listeners.BackupJobExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.ComputeBatchStepExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.FileBackupListener;
import org.ogerardin.b2b.batch.jobs.support.HashFilteringStrategy;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.PeerTarget;
import org.ogerardin.b2b.hash.HashProvider;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Job configuration for a backup job that processes a source of type {@link FilesystemSource}
 * and backs up to a network peer.
 */
@Configuration
public class FilesystemToPeerBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

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
                .start(peerInitBatchStep)    // step 0: init batch
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
    protected FilteringItemProcessor peerFilteringPathItemProcessor(
            @Qualifier("javaMD5Calculator") HashProvider hashProvider,
            @Value("#{jobParameters['backupset.id']}") String backupSetId
    ) {
        val fileBackupStatusInfoProvider = getFileBackupStatusInfoProvider(backupSetId);
        val filteringStrategy = new HashFilteringStrategy(fileBackupStatusInfoProvider, hashProvider);
        return new FilteringItemProcessor(fileBackupStatusInfoProvider, filteringStrategy);
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
            FilteringItemProcessor peerFilteringPathItemProcessor) {
        return new CompositeItemProcessor<LocalFileInfo, LocalFileInfo>() {
            {
                setDelegates(Arrays.asList(
                        countingProcessor,
                        //TODO insert HashingItemProcessor here
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
            PeerItemWriter peerWriter,
            FileBackupListener fileBackupListener
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
                // monitor progress
                .listener(fileBackupListener)
                .build();
    }



    /**
     * Provides a job-scoped {@link ItemWriter} that local files (designated by {@link LocalFileInfo} to the remote peer.
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
        val fileBackupStatusInfoProvider = getFileBackupStatusInfoProvider(backupSetId);
        return new PeerItemWriter(
                fileBackupStatusInfoProvider,
                targetHostname, targetPort);
    }


    @Bean
    @JobScope
    protected Step peerInitBatchStep(
            BackupJobContext jobContext
    ) {
        val fileBackupStatusInfoProvider = getFileBackupStatusInfoProvider(jobContext.getBackupSetId());
        return stepBuilderFactory.get("peerInitBatchStep")
                .tasklet(new InitBatchTasklet(fileBackupStatusInfoProvider, jobContext))
                .build();
    }
}
