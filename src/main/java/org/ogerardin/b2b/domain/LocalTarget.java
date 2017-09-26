package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LocalTarget extends BackupTarget {
    String path;

    public LocalTarget(String path) {
        this.path = path;
    }
}
