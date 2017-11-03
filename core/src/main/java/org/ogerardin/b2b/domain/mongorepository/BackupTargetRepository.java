package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.BackupTarget;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackupTargetRepository extends MongoRepository<BackupTarget, String> {

    BackupTarget findByName(String name);

}
