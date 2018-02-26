package org.ogerardin.b2b.config;

import lombok.Data;
import org.ogerardin.b2b.domain.ConfigItem;
import org.ogerardin.b2b.domain.mongorepository.ConfigItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Data
public class ConfigManager {

    private ConfigItem.MachineInfo machineInfo;

    @Autowired
    private ConfigItemRepository configItemRepository;

    public void init() {
        // get the stored computerId, if we don't have one assign a random one and sotre it
        machineInfo = (ConfigItem.MachineInfo) configItemRepository.findOne("machineInfo");
        if (machineInfo == null) {
            UUID uuid = UUID.randomUUID();
            machineInfo = new ConfigItem.MachineInfo("machineInfo", uuid.toString());
            machineInfo = configItemRepository.insert(machineInfo);
        }
    }
}
