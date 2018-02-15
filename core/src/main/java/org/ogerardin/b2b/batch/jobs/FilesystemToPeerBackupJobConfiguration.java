package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.PeerTarget;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Job implementation for a backup job that process a source of type {@link org.ogerardin.b2b.domain.FilesystemSource}
 * and backups to a network peer.
 */
@Configuration
public class FilesystemToPeerBackupJobConfiguration extends FilesystemSourceBackupJobConfiguration {

    private static final Log logger = LogFactory.getLog(FilesystemToPeerBackupJobConfiguration.class);

    public FilesystemToPeerBackupJobConfiguration() {
        addStaticParameter("target.type", PeerTarget.class.getName());
        addMandatoryParameter("target.hostname");
        addMandatoryParameter("target.port");
    }

    @Bean
    protected Job filesystemToPeerBackupJob(
            Step listFilesStep,
            Step backupToPeerStep,
            BackupJobExecutionListener jobListener) {
        return jobBuilderFactory
                .get(FilesystemToPeerBackupJobConfiguration.class.getSimpleName())
                .validator(getValidator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(listFilesStep) //step 1: list files and put them in the job context
                .next(backupToPeerStep)
                .build();
    }

    @Bean
    protected Step backupToPeerStep(
            ItemReader<Path> contextItemReader,
            PeerItemWriter peerWriter,
            PathItemProcessListener itemProcessListener)
    {
        return stepBuilderFactory.get("processLocalFiles")
                .<Path, Path>chunk(10)
                .reader(contextItemReader)
                .processor(new PassThroughItemProcessor<>()) //TODO should filter based on hash
                .writer(peerWriter)
                .listener(itemProcessListener)
                .build();
    }

    @Bean
    @JobScope
    protected PeerItemWriter peerItemWriter(
            @Value("#{jobParameters['target.hostname']}") String targetHostname,
            @Value("#{jobParameters['target.port']}") String targetPort

    ) {
        return new PeerItemWriter(targetHostname, targetPort);
    }

}
