package org.ogerardin.b2b.storage.gridfs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.storage.StorageProviderTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@DataMongoTest
public class GridFsStorageProviderTest extends StorageProviderTest {

    @Autowired
    private GridFsStorageProvider storageService;

    public GridFsStorageProviderTest() {
        setStorageService(storageService);
    }

    public void setStorageService(GridFsStorageProvider storageService) {
        this.storageService = storageService;
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