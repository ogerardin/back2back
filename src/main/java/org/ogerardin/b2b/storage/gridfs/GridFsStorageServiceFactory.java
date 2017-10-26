package org.ogerardin.b2b.storage.gridfs;

import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Component;

@Component
public class GridFsStorageServiceFactory implements StorageServiceFactory {

    @Autowired
    private MongoDbFactory mongoDbFactory;

    @Autowired
    private MongoConverter mongoConverter;

    @Override
    public StorageService getStorageService(String name) {
        return new GridFsStorageService(mongoDbFactory, mongoConverter, name);
    }
}
