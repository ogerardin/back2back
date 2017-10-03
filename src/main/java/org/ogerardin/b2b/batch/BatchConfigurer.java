package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.batch.mongodb.MongoJobRepositoryFactoryBean;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
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

    //TODO move somewhere else
    @Bean
    public BackupJobBuilder proxyBackupJobBuilder(BackupBuilderInvocationHandler invocationHandler) {
        BackupJobBuilder proxy =
                (BackupJobBuilder) Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{BackupJobBuilder.class},
                        invocationHandler
                );
        return proxy;
    }

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
