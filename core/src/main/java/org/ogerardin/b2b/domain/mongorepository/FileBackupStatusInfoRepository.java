package org.ogerardin.b2b.domain.mongorepository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A local Mongo repository used to store metadata about remotely backed up files.
 * Since we only care about the last hash, we use the file path as ID.
 */
public class FileBackupStatusInfoRepository
        extends SimpleMongoRepository<FileBackupStatusInfo, String>
    implements FileBackupStatusInfoProvider
{

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
        collection.updateMany(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"deleted\": \"true\"}}"));
    }

    @Override
    public void touch(Path path, Map<String, String> newHashes) {
        Optional<FileBackupStatusInfo> maybeStatusInfo = getLatestStoredRevision(path);
        FileBackupStatusInfo statusInfo = maybeStatusInfo.orElseGet(FileBackupStatusInfo::new);
        statusInfo.setDeleted(false);
        statusInfo.setCurrentHashes(newHashes);
        saveRevisionInfo(statusInfo);
    }

    @Override
    public void saveRevisionInfo(FileBackupStatusInfo revision) {
        super.save(revision);
    }

    @Override
    public ItemReader<FileBackupStatusInfo> reader() {
        return new MongoItemReaderBuilder<FileBackupStatusInfo>()
                .template(mongoOperations)
                .saveState(false)
                .collection(entityInformation.getCollectionName())
                .targetType(FileBackupStatusInfo.class)
                .query(new Query().limit(Integer.MAX_VALUE)) // return all items
                .sorts(new HashMap<>())
                .pageSize(10)  // fetch N at a time
                .build();
    }

    @Override
    public void deletedDeleted() {
        MongoCollection<Document> collection = mongoOperations.getCollection(entityInformation.getCollectionName());
        collection.deleteMany(BasicDBObject.parse("{ \"$eq\": {\"deleted\": \"true\"}}"));

    }

}
