package org.ogerardin.b2b.storage.gridfs;

import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Component;

@Component
public class GridFsStorageServiceFactory implements StorageServiceFactory<GridFsStorageService> {

    @Autowired
    private MongoDbFactory mongoDbFactory;

    @Autowired
    private MongoConverter mongoConverter;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public GridFsStorageService getStorageService(String id) {
        // The specified id is used as the GridFS bucket name so that all the files backed up as part of a
        // backupSet are stored in a distinct bucket (see https://docs.mongodb.com/manual/core/gridfs/index.html#gridfs-collections)
        return new GridFsStorageService(mongoDbFactory, mongoConverter, mongoTemplate, id);
    }
}
