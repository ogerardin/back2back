package org.ogerardin.b2b.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.ConfigItem;
import org.ogerardin.b2b.domain.mongorepository.ConfigItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Data
@Slf4j
public class ConfigManager {

    public static final String KEY_MACHINE_INFO = "machineInfo";

    private ConfigItem.MachineInfo machineInfo;

    @Autowired
    private ConfigItemRepository configItemRepository;

    public void init() {
        // get the stored computerId, if we don't have one assign a random one and sotre it
        machineInfo = (ConfigItem.MachineInfo) configItemRepository.findOne(KEY_MACHINE_INFO);
        if (machineInfo == null) {
            UUID uuid = UUID.randomUUID();
            machineInfo = new ConfigItem.MachineInfo(KEY_MACHINE_INFO, uuid.toString());
            machineInfo = configItemRepository.insert(machineInfo);
        }
        log.info("Machine ID: " + machineInfo.getComputerId());
    }
}
