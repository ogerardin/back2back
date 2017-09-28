package org.ogerardin.b2b.batch;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;

@Configuration
@EnableBatchProcessing
public class BatchConfigurer extends DefaultBatchConfigurer {

    private final AsyncTaskExecutor asyncTaskExecutor;

    @Autowired
    public BatchConfigurer(AsyncTaskExecutor asyncTaskExecutor) {
        this.asyncTaskExecutor = asyncTaskExecutor;
    }


    @Override
    public JobLauncher createJobLauncher() throws Exception {
        JobLauncher jobLauncher = super.createJobLauncher();
        // we assume DefaultBatchConfigurer.createJobLauncher returns a SimpleJobLauncher because
        // we want to change the TaskExecutor to an AsyncTaskExecutor.
        ((SimpleJobLauncher)jobLauncher).setTaskExecutor(asyncTaskExecutor);
        return jobLauncher;
    }

}
