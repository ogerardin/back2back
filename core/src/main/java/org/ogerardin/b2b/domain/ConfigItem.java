package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Generic configuration item
 */
@Document(collection = "config")
@Data
public abstract class ConfigItem {

    @Id
    String name;

    ConfigItem(@NonNull String name) {
        this.name = name;
    }

    /**
     * Information about the local machine
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class MachineInfo extends ConfigItem {
        /** Randomly assigned UUID identifying this computer */
        String computerId;
        public MachineInfo(String name, String computerId) {
            super(name);
            this.computerId = computerId;
        }
    }

}

