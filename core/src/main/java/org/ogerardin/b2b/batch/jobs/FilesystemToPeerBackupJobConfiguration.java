package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.jobs.listeners.BackupJobExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.ComputeBatchStepExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.FileBackupListener;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.PeerTarget;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;

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
            ItemProcessor<LocalFileInfo, LocalFileInfo> countingAndFilteringItemProcessor,
            ComputeBatchStepExecutionListener computeBatchStepExecutionListener) {
        return stepBuilderFactory
                .get("peerComputeBatchStep")
                .<LocalFileInfo, LocalFileInfo> chunk(10)
                // read files from local filesystem
                .reader(filesystemItemReader)
                // filter out files that don't need backup
                .processor(countingAndFilteringItemProcessor)
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
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider
    ) throws MalformedURLException {
        if (targetPort == null) {
            targetPort = properties.getDefaultPeerPort();
        }
        return new PeerItemWriter(
                fileBackupStatusInfoProvider,
                targetHostname, targetPort);
    }


    @Bean
    @JobScope
    protected Step peerInitBatchStep(
            BackupJobContext jobContext,
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider
    ) {
        return stepBuilderFactory.get("peerInitBatchStep")
                .tasklet(new InitBatchTasklet(fileBackupStatusInfoProvider, jobContext))
                .build();
    }
}
