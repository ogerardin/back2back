package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RemoteTarget extends BackupTarget {
    String name;
    String hostname;
    long port;
}
