package org.ogerardin.b2b.batch.jobs;

import lombok.val;
import org.ogerardin.b2b.batch.jobs.listeners.BackupJobExecutionListener;
import org.ogerardin.b2b.batch.jobs.listeners.FileBackupListener;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.entity.PeerTarget;
import org.ogerardin.b2b.storage.peer.PeerStorageService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;

/**
 * Job configuration for a backup job that processes a source of type {@link FilesystemSource}
 * and backs up to a network peer.
 */
@Configuration
public class FilesystemToPeerBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

    @Autowired
    ConfigManager configManager;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();


    public FilesystemToPeerBackupJobConfiguration() {
        addStaticParameter("target.type", PeerTarget.class.getName());
        addMandatoryParameter("target.hostname");
//        addMandatoryParameter("target.port");
    }

    @Bean
    protected Job filesystemToPeerBackupJob(
            Step initBatchStep,
            Step computeBatchStep,
            Step peerBackupStep,
            BackupJobExecutionListener jobListener,
            Step finalizeBackupStep
    ) {
        return jobBuilderFactory
                .get("filesystemToPeerBackupJob")
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(initBatchStep)
                .next(computeBatchStep)
                .next(peerBackupStep)
                .next(finalizeBackupStep)
                .build();
    }

    /**
     * Provides a {@link Step} that writes items from the current batch by storing them to the remote peer
     */
    @Bean
    @JobScope
    protected Step peerBackupStep(
            FileBackupListener fileBackupListener,
            FileBackupStatusInfoProvider fileBackupStatusInfoProvider,
            BackupItemProcessor peerBackupItemProcessor,
            BackupStatusUpdater backupStatusUpdater) {
        return stepBuilderFactory
                .get("backupToPeerStorageStep")
                // handle 1 file at a time
                .<FileBackupStatusInfo, FileBackupStatusInfo> chunk(1)
                // read files from local backup status database
                .reader(fileBackupStatusInfoProvider.reader())
                // perform backup
                .processor(peerBackupItemProcessor)
                // store backup status
                .writer(backupStatusUpdater)
                // monitor progress
                .listener(fileBackupListener)
                .build();
    }


    /**
     * Provides a job-scoped {@link ItemWriter} that local files (designated by {@link LocalFileInfo} to the remote peer.
     */
    @Bean
    @JobScope
    protected BackupItemProcessor peerBackupItemProcessor(
            @Value("#{jobParameters['target.hostname']}") String targetHostname,
            @Value("#{jobParameters['target.port']}") Integer targetPort
    ) throws MalformedURLException {
        if (targetPort == null) {
            targetPort = properties.getDefaultPeerPort();
        }
        String computerId = configManager.getMachineInfo().getComputerId();
        val storageService = new PeerStorageService(targetHostname, targetPort, computerId, REST_TEMPLATE);

        return new BackupItemProcessor(
                storageService,
                properties.getFileThrottleDelay());
    }


}
