package org.ogerardin.b2b;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "org.ogerardin.b2b")
@Data
public class B2BProperties {

    int defaultPeerPort = 80;

    //
    // Debug properties
    //

    /** Delay between files (for testing) */
    long fileThrottleDelay = 1000;

    /** Automatically restart full backup job when finished */
    boolean continuousBackup = true;

    /** Pause between backup jobs */
    long pauseAfterBackup = 30 * 60 * 1000;

    /** Start backup jobs automatically */
    boolean startJobs = true;
}
