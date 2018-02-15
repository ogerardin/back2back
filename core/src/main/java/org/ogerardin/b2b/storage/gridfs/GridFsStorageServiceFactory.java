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
    public GridFsStorageService getStorageService(String backupSetId) {
        // we use the backupSetId as the bucket name used by GridFS so that all the files backed up as part of a
        // backupSet are stored in a distinct bucket
        // TODO we should implement a maintenance job to delete buckets for which there is no backupSet
        return new GridFsStorageService(mongoDbFactory, mongoConverter, mongoTemplate, backupSetId);
    }
}
