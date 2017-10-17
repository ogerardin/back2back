package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.BackupSource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackupSourceRepository extends MongoRepository<BackupSource, String> {

    BackupSource findByName(String name);

}
