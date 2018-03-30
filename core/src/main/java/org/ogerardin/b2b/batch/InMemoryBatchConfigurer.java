package org.ogerardin.b2b.batch;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Provides a customized Spring batch environment with the following features:
 * - the {@link JobLauncher} is configured to use an {@link AsyncTaskExecutor}
 * - the {@link JobRepository} uses an in-memory implementation
 */
@Component
@Configuration
@EnableBatchProcessing
public class InMemoryBatchConfigurer extends DefaultBatchConfigurer {

    private final AsyncTaskExecutor asyncTaskExecutor;

    public InMemoryBatchConfigurer(AsyncTaskExecutor asyncTaskExecutor) {
        this.asyncTaskExecutor = asyncTaskExecutor;
    }


    protected JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.setTaskExecutor(asyncTaskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

}
