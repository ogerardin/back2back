package org.ogerardin.b2b.storage.gridfs;

import org.junit.runner.RunWith;
import org.ogerardin.b2b.storage.StorageProviderTest;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataMongoTest
//@SpringBootTest
public class GridFsStorageProviderTest extends StorageProviderTest{



    @Autowired
    public GridFsStorageProviderTest(@Qualifier("gridFsStorageProvider") StorageService storageService) {
        super(storageService);
    }

}