package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.StoredFileVersionInfo;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import java.util.Optional;

public class PeerFileVersionInfoRepository
        extends SimpleMongoRepository<StoredFileVersionInfo, String>
    implements StoredFileVersionInfoProvider
{

    public PeerFileVersionInfoRepository(MongoEntityInformation<StoredFileVersionInfo, String> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
    }

    @Override
    public Optional<StoredFileVersionInfo> getStoredFileVersionInfo(String path) {
        StoredFileVersionInfo info = findOne(path);
        return Optional.ofNullable(info);
    }
}
