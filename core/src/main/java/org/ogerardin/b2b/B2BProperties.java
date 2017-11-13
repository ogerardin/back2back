package org.ogerardin.b2b;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "org.ogerardin.b2b")
@Data
public class B2BProperties {

    //
    // Debug properties
    //

    /** Delay between files */
    long fileThrottleDelay = 1000;

    /** Automatically restart full backup job when finished */
    boolean autorestart = false;

    /** Pause between backup jobs */
    long pauseAfterBackup = 30000;
}
