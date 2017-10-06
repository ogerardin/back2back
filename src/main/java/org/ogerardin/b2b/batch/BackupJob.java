package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
public abstract class BackupJob {

    private static final Log logger = LogFactory.getLog(BackupJob.class);

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;
    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    protected JobExecutionListener listener() {
        return new JobExecutionListenerSupport() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                logger.info("About to start backup job: " + jobExecution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                logger.info("Backup job " + jobExecution.getJobInstance().getJobName() +
                        " terminated with status: " + jobExecution.getStatus());
            }
        };
    }
}
