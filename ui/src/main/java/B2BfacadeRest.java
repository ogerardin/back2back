import com.fasterxml.jackson.databind.Module;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.jackson.JacksonConfig;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class B2BfacadeRest implements B2Bfacade {

    private static RestTemplate restTemplate = new RestTemplate();

    static {
        // register custom Jackson configuration from the core project
        Module module = JacksonConfig.customModule();
        registerModule(restTemplate, module, MappingJackson2HttpMessageConverter.class);
    }

    @Override
    public BackupSet[] getBackupSets() {
        BackupSet[] backupSets = restTemplate.getForObject("http://localhost:8080/api/backupsets", BackupSet[].class);
        return backupSets;
    }

    private static void registerModule(RestTemplate restTemplate, Module module,
                                       Class<? extends AbstractJackson2HttpMessageConverter> converterClass) {
        restTemplate.getMessageConverters().stream()
                .filter(converterClass::isInstance)
                .map(converterClass::cast)
                .forEach(mc -> mc.getObjectMapper().registerModule(module));
    }
}
