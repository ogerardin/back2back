package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackupSetRepository extends MongoRepository<BackupSet, String> {
}
