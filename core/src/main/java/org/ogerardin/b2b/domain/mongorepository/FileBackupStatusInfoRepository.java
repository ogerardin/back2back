package org.ogerardin.b2b.domain.mongorepository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * A local Mongo repository used to store metadata about remotely backed up files.
 * Since we only care about the last hash, we use the file path as ID.
 */
public class FileBackupStatusInfoRepository
        extends SimpleMongoRepository<FileBackupStatusInfo, String>
        implements FileBackupStatusInfoProvider {

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<FileBackupStatusInfo, String> entityInformation;

    public FileBackupStatusInfoRepository(MongoEntityInformation<FileBackupStatusInfo, String> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    @Override
    public Optional<FileBackupStatusInfo> getLatestStoredRevision(String path) {
        return findById(path);
    }

    @Override
    public void untouchAll() {
        MongoCollection<Document> collection = mongoOperations.getCollection(entityInformation.getCollectionName());
        collection.updateMany(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"deleted\": true}}"));
    }

    @Override
    public void saveStatusInfo(FileBackupStatusInfo statusInfo) {
        super.save(statusInfo);
    }

    @Override
    public ItemReader<FileBackupStatusInfo> backupRequestedItemReader() {
        return new MongoItemReaderBuilder<FileBackupStatusInfo>()
                .template(mongoOperations)
                .saveState(false)
                .collection(entityInformation.getCollectionName())
                .targetType(FileBackupStatusInfo.class)
                // retrieve items where backupRequested == true or deleted == true
                .query(new Query(new Criteria().orOperator(
                        where("deleted").is(Boolean.TRUE),
                        where("backupRequested").is(Boolean.TRUE)
                    )
                )
                .limit(Integer.MAX_VALUE))
                // no sorting necessary
                .sorts(new HashMap<>())
                .pageSize(10)  // fetch N at a time
                .build();
    }

    @Override
    public long removeDeleted() {
        MongoCollection<Document> collection = mongoOperations.getCollection(entityInformation.getCollectionName());
        DeleteResult deleteResult = collection.deleteMany(BasicDBObject.parse("{ \"deleted\": {\"$eq\": true}}"));
        return deleteResult.getDeletedCount();
    }

    @Override
    public String[] deletedFiles() {
        List<FileBackupStatusInfo> deleted = mongoOperations.find(
                        query(where("deleted").is(Boolean.TRUE)),
                        FileBackupStatusInfo.class,
                        entityInformation.getCollectionName()
                );
        return deleted
                .stream()
                .map(FileBackupStatusInfo::getPath)
                .toArray(String[]::new);

    }

}
