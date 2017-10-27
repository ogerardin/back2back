package org.ogerardin.b2b.storage.gridfs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.storage.StorageProviderTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataMongoTest
@ContextConfiguration(classes = )
public class GridFsStorageProviderTest extends StorageProviderTest<GridFsStorageService> {

    @Autowired
    GridFsStorageServiceFactory gridFsStorageServiceFactory;

    public GridFsStorageProviderTest() {
        GridFsStorageService storageService = gridFsStorageServiceFactory.getStorageService("test");
        setStorageService(storageService);
    }

    @Before
    public void setUp() {
        storageService.init();
        storageService.deleteAll();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void loadAll() throws Exception {
        super.testLoadAll(storageService);
    }


}