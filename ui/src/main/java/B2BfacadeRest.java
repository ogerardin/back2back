import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.jackson.JacksonConfig;
import org.springframework.web.client.RestTemplate;

public class B2BfacadeRest implements B2Bfacade {

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        Module module = JacksonConfig.customSerializationModule();
        objectMapper.registerModule(module);
    }

    @Override
    public BackupSet[] getBackupSets() {
        RestTemplate restTemplate = new RestTemplate();
        BackupSet[] backupSets = restTemplate.getForObject("http://localhost:8080/api/backupsets", BackupSet[].class);
        return backupSets;
    }
}
