package org.ogerardin.b2b.batch.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent class for all DAO used to store SpringBatch Infrastructure data to Mongo DB.
 *
 * @author vfouzdar
 * @author Baruch S.
 */
@Component
public abstract class AbstractMongoDao {

    static final String VERSION_KEY = "version";
    static final String START_TIME_KEY = "startTime";
    static final String END_TIME_KEY = "endTime";
    static final String EXIT_CODE_KEY = "exitCode";
    static final String EXIT_MESSAGE_KEY = "exitMessage";
    static final String LAST_UPDATED_KEY = "lastUpdated";
    static final String STATUS_KEY = "status";

    // Dot escaping
    static final String DOT_ESCAPE_STRING = "\\{dot\\}";
    static final String DOT_STRING = "\\.";

    // Job Constants    
    static final String JOB_NAME_KEY = "jobName";
    static final String JOB_INSTANCE_ID_KEY = "jobInstanceId";
    static final String JOB_KEY_KEY = "jobKey";
    static final String JOB_PARAMETERS_KEY = "jobParameters";

    // Job Execution Constants
    static final String JOB_EXECUTION_ID_KEY = "jobExecutionId";
    static final String CREATE_TIME_KEY = "createTime";

    // Job Execution Contexts Constants
    static final String STEP_EXECUTION_ID_KEY = "stepExecutionId";
    static final String TYPE_SUFFIX = "_TYPE";

    // Step Execution Constants
    static final String STEP_NAME_KEY = "stepName";
    static final String COMMIT_COUNT_KEY = "commitCount";
    static final String READ_COUNT_KEY = "readCount";
    static final String FILTER_COUT_KEY = "filterCout";
    static final String WRITE_COUNT_KEY = "writeCount";
    static final String READ_SKIP_COUNT_KEY = "readSkipCount";
    static final String WRITE_SKIP_COUNT_KEY = "writeSkipCount";
    static final String PROCESS_SKIP_COUT_KEY = "processSkipCout";
    static final String ROLLBACK_COUNT_KEY = "rollbackCount";

    // Sequences
    private static final String SEQUENCES_COLLECTION_NAME = "Sequences";
    private static final String ID_KEY = "_id";
    private static final String NS_KEY = "_ns";

    @Autowired
    protected MongoTemplate mongoTemplate;

    private String prefix = "batch";

    private final String collectionName;


    protected AbstractMongoDao(String collectionName) {
        this.collectionName = getPrefixedCollectionName(collectionName);
    }

    private String getPrefixedCollectionName(String collectionName) {
        return prefix.isEmpty() ? collectionName : (prefix + "_" + collectionName);
    }

    public AbstractMongoDao(Class clazz) {
        this(clazz.getSimpleName());
    }

    final DBCollection getCollection(){
        return mongoTemplate.getCollection(collectionName);
    }

    Long getNextId(String name, MongoTemplate mongoTemplate) {
        DBCollection collection = mongoTemplate.getDb().getCollection(getPrefixedCollectionName(SEQUENCES_COLLECTION_NAME));
        BasicDBObject sequence = new BasicDBObject("name", name);
        collection.update(sequence, new BasicDBObject("$inc", new BasicDBObject("value", 1L)), true, false);
        return (Long) collection.findOne(sequence).get("value");
    }

    void removeSystemFields(DBObject dbObject) {
        dbObject.removeField(ID_KEY);
        dbObject.removeField(NS_KEY);
    }

    BasicDBObject jobInstanceIdObj(Long id) {
        return new BasicDBObject(MongoJobInstanceDao.JOB_INSTANCE_ID_KEY, id);
    }

    BasicDBObject jobExecutionIdObj(Long id) {
        return new BasicDBObject(JOB_EXECUTION_ID_KEY, id);
    }

    @SuppressWarnings({"unchecked"})
    JobParameters getJobParameters(Long jobInstanceId, MongoTemplate mongoTemplate) {
        DBObject jobParamObj = mongoTemplate
                .getCollection(JobInstance.class.getSimpleName())
                .findOne(new BasicDBObject(jobInstanceIdObj(jobInstanceId)));

        if (jobParamObj != null && jobParamObj.get(MongoJobInstanceDao.JOB_PARAMETERS_KEY) != null) {

            Map<String, ?> jobParamsMap = (Map<String, ?>) jobParamObj.get(MongoJobInstanceDao.JOB_PARAMETERS_KEY);

            Map<String, JobParameter> map = new HashMap<String, JobParameter>(
                    jobParamsMap.size());
            for (Map.Entry<String, ?> entry : jobParamsMap.entrySet()) {
                Object param = entry.getValue();
                String key = entry.getKey().replaceAll(DOT_ESCAPE_STRING, DOT_STRING);
                if (param instanceof String) {
                    map.put(key, new JobParameter((String) param));
                } else if (param instanceof Long) {
                    map.put(key, new JobParameter((Long) param));
                } else if (param instanceof Double) {
                    map.put(key, new JobParameter((Double) param));
                } else if (param instanceof Date) {
                    map.put(key, new JobParameter((Date) param));
                } else {
                    map.put(key, null);
                }
            }
            return new JobParameters(map);
        }
        return null;
    }
}
