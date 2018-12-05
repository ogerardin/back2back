package org.ogerardin.b2b.storage.peer;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.storage.StorageProviderTest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PeerStorageProviderTest extends StorageProviderTest<PeerStorageService> implements InitializingBean {


    @Autowired
    private TestRestTemplate restTemplate;

    public PeerStorageProviderTest() {
    }

    @Override
    public void setUp() {
        // no need to delete all
    }

    @Override
    public void tearDown() {
        // no need to delete all
    }

    @Test
    public void testStoreAndRetrieveById() throws Exception {
        super.testStoreAndRetrieveById();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        String computerId = UUID.randomUUID().toString();
        val storageService = new PeerStorageService("localhost", 8080, computerId, restTemplate.getRestTemplate());
        setStorageService(storageService);
        storageService.init();
    }
}
