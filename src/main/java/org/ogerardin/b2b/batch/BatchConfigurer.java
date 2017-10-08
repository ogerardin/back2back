package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.batch.mongodb.MongoJobRepositoryFactoryBean;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import java.lang.reflect.Proxy;

/**
 * Provides a customized Spring batch environment. The main customization is to replace the default
 * synchronous {@link org.springframework.core.task.TaskExecutor} with an Asynchronous one.
 */
@Component
@Configuration
@EnableBatchProcessing
public class BatchConfigurer implements org.springframework.batch.core.configuration.annotation.BatchConfigurer {

    private final AsyncTaskExecutor asyncTaskExecutor;

    private PlatformTransactionManager transactionManager;
    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private JobExplorer jobExplorer;

    @Autowired
    private MongoJobRepositoryFactoryBean mongoJobRepositoryFactoryBean;

    @Autowired
    public BatchConfigurer(AsyncTaskExecutor asyncTaskExecutor) {
        this.asyncTaskExecutor = asyncTaskExecutor;
    }


    private JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(asyncTaskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @PostConstruct
    public void initialize() throws Exception {
        this.jobRepository = mongoJobRepositoryFactoryBean.getObject();
        this.transactionManager = mongoJobRepositoryFactoryBean.getTransactionManager();
        this.jobLauncher = createJobLauncher();
        this.jobExplorer = createJobExplorer();

        fixJobRepository();
    }

    /**
     * If using en embedded instance of MongoDB, when shutting down the app it might
     * be stopped before the status of running jobs is updated, leaving
     * them in bogus STARTED status. If there are such jobExecutions, set
     * them to FAILED status.
     */
    private void fixJobRepository() {
        for (String jobName : jobExplorer.getJobNames()) {
            for (JobInstance jobInstance : jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE)) {
                for (JobExecution jobExecution : jobExplorer.getJobExecutions(jobInstance)) {
                    if (jobExecution.getStatus() == BatchStatus.STARTED) {
                        jobExecution.setStatus(BatchStatus.FAILED);
                    }
                }
            }
        }
    }

    private JobExplorer createJobExplorer() {
        return new SimpleJobExplorer(
                mongoJobRepositoryFactoryBean.getJobInstanceDao(),
                mongoJobRepositoryFactoryBean.getJobExecutionDao(),
                mongoJobRepositoryFactoryBean.getStepExecutionDao(),
                mongoJobRepositoryFactoryBean.getExecutionContextDao()
                );
    }

    @Override
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() {
        return jobExplorer;
    }


}
