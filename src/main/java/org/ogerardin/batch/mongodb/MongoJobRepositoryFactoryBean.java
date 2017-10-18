package org.ogerardin.batch.mongodb;


import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.AbstractJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Job Factory responsible to provide execution/Job DAOs for storing Springbatch metadata in Mongo DB. <br/>
 * Following DAO needs to be set as property during bean definition <br/>
 * <ul>
 * <li>{@link MongoExecutionContextDao}</li>
 * <li>{@link MongoJobExecutionDao}</li>
 * <li>{@link MongoJobInstanceDao}</li>
 * <li>{@link MongoStepExecutionDao}</li>
 * </ul>
 *
 * @author vfouzdar
 * @author ogerardin
 */
@Component
public class MongoJobRepositoryFactoryBean extends AbstractJobRepositoryFactoryBean implements InitializingBean {

    @Autowired
    private MongoExecutionContextDao executionContextDao;
    @Autowired
    private MongoJobExecutionDao jobExecutionDao;
    @Autowired
    private MongoJobInstanceDao jobInstanceDao;
    @Autowired
    private MongoStepExecutionDao stepExecutionDao;

    public MongoJobRepositoryFactoryBean() {
        setTransactionManager(new ResourcelessTransactionManager());
    }

    @Override
    protected JobInstanceDao createJobInstanceDao() {
        return jobInstanceDao;
    }

    @Override
    protected JobExecutionDao createJobExecutionDao() {
        return jobExecutionDao;
    }

    @Override
    protected StepExecutionDao createStepExecutionDao() {
        return stepExecutionDao;
    }

    @Override
    protected ExecutionContextDao createExecutionContextDao() {
        return executionContextDao;
    }

    public MongoExecutionContextDao getExecutionContextDao() {
        return executionContextDao;
    }

    public MongoJobExecutionDao getJobExecutionDao() {
        return jobExecutionDao;
    }

    public MongoJobInstanceDao getJobInstanceDao() {
        return jobInstanceDao;
    }

    public MongoStepExecutionDao getStepExecutionDao() {
        return stepExecutionDao;
    }
}
