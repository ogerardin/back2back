package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NetworkTarget extends BackupTarget {
    String hostname;
    long port;

    public NetworkTarget(String hostname, long port) {
        this.hostname = hostname;
        this.port = port;
    }
}
