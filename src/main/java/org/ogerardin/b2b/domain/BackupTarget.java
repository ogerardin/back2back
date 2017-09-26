package org.ogerardin.b2b.domain;

import lombok.Data;

@Data
public abstract class BackupTarget {
    String id;
    private String name;
}
