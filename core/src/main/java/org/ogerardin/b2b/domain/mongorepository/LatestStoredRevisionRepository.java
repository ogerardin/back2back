package org.ogerardin.b2b.domain.mongorepository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.LatestStoredRevision;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A local Mongo repository used to store metadata about remotely backed up files.
 * Since we only care about the last hash, we use the file path as ID.
 */
public class LatestStoredRevisionRepository
        extends SimpleMongoRepository<LatestStoredRevision, String>
    implements LatestStoredRevisionProvider
{

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<LatestStoredRevision, String> entityInformation;

    public LatestStoredRevisionRepository(MongoEntityInformation<LatestStoredRevision, String> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    @Override
    public Optional<LatestStoredRevision> getLatestStoredRevision(String path) {
        return findById(path);
    }

    @Override
    public void untouchAll() {
        MongoCollection<Document> collection = mongoOperations.getCollection(entityInformation.getCollectionName());
        collection.updateMany(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"deleted\": \"true\"}}"));
    }

    @Override
    public boolean touch(Path path) {
        Optional<LatestStoredRevision> maybeVersionInfo = getLatestStoredRevision(path);
        if (! maybeVersionInfo.isPresent()) {
            return false;
        }
        LatestStoredRevision versionInfo = maybeVersionInfo.get();
        versionInfo.setDeleted(false);
        saveRevisionInfo(versionInfo);
        return true;
    }

    @Override
    public void saveRevisionInfo(LatestStoredRevision revision) {
        super.save(revision);
    }

//    @Override
//    public long deletedCount() {
//        //FIXME poor implementation
//        return findAll().stream()
//                .filter(LatestStoredRevision::isDeleted)
//                .count();
//    }

}
