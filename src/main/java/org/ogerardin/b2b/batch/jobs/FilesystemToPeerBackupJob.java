package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.PeerTarget;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
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
public class FilesystemToPeerBackupJob extends FilesystemSourceBackupJob {

    private static final Log logger = LogFactory.getLog(FilesystemToPeerBackupJob.class);

    public FilesystemToPeerBackupJob() {
        addStaticParameter("target.type", PeerTarget.class.getName());
        addMandatoryParameter("target.hostname");
        addMandatoryParameter("target.port");
    }

    @Bean("localToPeerJob")
    protected Job localToPeerBackupJob(Step localToPeerStep, JobExecutionListener jobListener) {
        return jobBuilderFactory.get(FilesystemToPeerBackupJob.class.getSimpleName())
                .validator(validator())
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .flow(localToPeerStep)
                .end()
                .build();
    }

    @Bean("localToPeerStep")
    protected Step step(ItemProcessor<Path, Path> localToPeerItemProcessor, ItemReader<Path> localToPeerItemReader) {
        return stepBuilderFactory.get("processLocalFiles")
                .<Path, Path>chunk(10)
                .reader(localToPeerItemReader)
                .processor(localToPeerItemProcessor)
//                .writer(newWriter())
                .build();
    }

    @Bean(name = "localToPeerItemProcessor")
    protected ItemProcessor<Path, Path> itemProcessor() {
        //TODO do something useful
        return new PassThroughItemProcessor<>();
    }

    @Bean(name = "localToPeerItemReader")
    @StepScope
    protected ItemReader<Path> itemReader(@Value("#{jobParameters['source.root']}") String root) {
        //FIXME this is for debug
        return () -> null;
    }

}
