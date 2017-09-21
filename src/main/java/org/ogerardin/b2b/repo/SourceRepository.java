package org.ogerardin.b2b.repo;

import org.ogerardin.b2b.domain.Source;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SourceRepository extends MongoRepository<Source, String> {

}
