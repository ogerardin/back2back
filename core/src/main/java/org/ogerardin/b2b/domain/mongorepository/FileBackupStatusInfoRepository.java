package org.ogerardin.b2b.domain.mongorepository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.nio.file.Path;
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
    public boolean touch(Path path) {
        Optional<FileBackupStatusInfo> maybeVersionInfo = getLatestStoredRevision(path);
        if (! maybeVersionInfo.isPresent()) {
            return false;
        }
        FileBackupStatusInfo versionInfo = maybeVersionInfo.get();
        versionInfo.setDeleted(false);
        saveRevisionInfo(versionInfo);
        return true;
    }

    @Override
    public void saveRevisionInfo(FileBackupStatusInfo revision) {
        super.save(revision);
    }

//    @Override
//    public long deletedCount() {
//        //FIXME poor implementation
//        return findAll().stream()
//                .filter(FileBackupStatusInfo::isDeleted)
//                .count();
//    }

}
