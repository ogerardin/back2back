package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.ConfigItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigItemRepository extends MongoRepository<ConfigItem, String> {
}
