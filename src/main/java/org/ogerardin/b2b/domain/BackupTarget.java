package org.ogerardin.b2b.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "targets")
@Data
// include concrete type information as a pseudo-property "_class" on serialized JSON
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_class")
public abstract class BackupTarget implements JobParametersPopulator {

    @Id
    private String id;

    private String name;
}
