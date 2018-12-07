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

}
