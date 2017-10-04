package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "targets")
@Data
public abstract class BackupTarget implements JobParametersPopulator {

    @Id
    private String id;

    private String name;
}
