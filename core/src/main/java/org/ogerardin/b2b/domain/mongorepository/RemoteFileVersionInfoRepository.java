package org.ogerardin.b2b.domain.mongorepository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A local Mongo repository used to store metadata about remotely backed up files.
 */
public class RemoteFileVersionInfoRepository
        extends SimpleMongoRepository<StoredFileVersionInfo, String>
    implements StoredFileVersionInfoProvider
{

    private final MongoOperations mongoOperations;
    private final MongoEntityInformation<StoredFileVersionInfo, String> entityInformation;

    public RemoteFileVersionInfoRepository(MongoEntityInformation<StoredFileVersionInfo, String> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityInformation = metadata;
    }

    @Override
    public Optional<StoredFileVersionInfo> getStoredFileVersionInfo(String path) {
        StoredFileVersionInfo info = findOne(path);
        return Optional.ofNullable(info);
    }

    @Override
    public void untouchAll() {
        DBCollection collection = mongoOperations.getCollection(entityInformation.getCollectionName());
        collection.updateMulti(new BasicDBObject(), BasicDBObject.parse("{ \"$set\": {\"deleted\": \"true\"}}"));
    }

    @Override
    public boolean touch(Path path) {
        Optional<StoredFileVersionInfo> maybeVersionInfo = getStoredFileVersionInfo(path);
        if (! maybeVersionInfo.isPresent()) {
            return false;
        }
        StoredFileVersionInfo versionInfo = maybeVersionInfo.get();
        versionInfo.setDeleted(false);
        save(versionInfo);
        return true;
    }
}
