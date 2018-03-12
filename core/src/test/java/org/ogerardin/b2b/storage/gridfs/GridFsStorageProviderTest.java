package org.ogerardin.b2b.storage.gridfs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.storage.StorageProviderTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataMongoTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GridFsStorageProviderTest extends StorageProviderTest<GridFsStorageService> {

    @Autowired
    private MongoDbFactory mongoDbFactory;

    @Autowired
    private MongoConverter mongoConverter;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setUp() {
        GridFsStorageService storageService = new GridFsStorageService(mongoDbFactory, mongoConverter, mongoTemplate, "test");
        storageService.init();
        storageService.deleteAll();
        setStorageService(storageService);
    }

    @After
    public void tearDown() {
    }


    @Test
    public void loadAll() throws Exception {
        super.testLoadAll(storageService);
    }


}