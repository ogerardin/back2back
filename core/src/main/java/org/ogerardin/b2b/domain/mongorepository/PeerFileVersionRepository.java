package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.PeerFileVersion;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

public class PeerFileVersionRepository extends SimpleMongoRepository<PeerFileVersion, String> {

    public PeerFileVersionRepository(MongoEntityInformation<PeerFileVersion, String> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
    }
}
