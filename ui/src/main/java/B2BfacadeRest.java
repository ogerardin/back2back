import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.web.client.RestTemplate;

public class B2BfacadeRest implements B2Bfacade {

    @Override
    public BackupSet[] getBackupSets() {
        RestTemplate restTemplate = new RestTemplate();
        BackupSet[] backupSets = restTemplate.getForObject("http://localhost:8080/api/backupsets", BackupSet[].class);
        return backupSets;
    }
}
