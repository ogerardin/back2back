package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class BackupSource {

    @Id
    String id;
}
