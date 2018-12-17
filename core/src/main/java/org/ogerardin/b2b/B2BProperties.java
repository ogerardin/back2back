package org.ogerardin.b2b;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "org.ogerardin.b2b")
@Data
public class B2BProperties {

    int defaultPeerPort = 80;

    //
    // Debug properties
    //

    /** Introduce delay between files */
    long fileThrottleDelay = 1000;

    /** Pause between backup jobs */
    long pauseAfterBackup = 30 * 60 * 1000;

    /**
     * Number of files processed in one chunk during the "compute batch" phase.
     * This affects how many hashes are saved in the hash database at a time, and also how often statistics are saved
     * to the backup set.
     */
    int listFilesChunkSize = 10;

}
