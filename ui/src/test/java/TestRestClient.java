import org.junit.Ignore;
import org.junit.Test;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.web.client.RestTemplate;

public class TestRestClient {

    @Ignore
    @Test
    public void testRest() {
        RestTemplate restTemplate = new RestTemplate();
        BackupSet[] backupSets = restTemplate.getForObject("http://localhost:8080/api/backupsets", BackupSet[].class);

    }
}
