package org.ogerardin.b2b.config;

import org.ogerardin.b2b.domain.BackupTarget;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackupTargetRepository extends MongoRepository<BackupTarget, String> {

}
