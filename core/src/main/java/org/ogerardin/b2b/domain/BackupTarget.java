package org.ogerardin.b2b.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a backup destination.
 */
@Document(collection = "targets")
@Data
// Instructs Jackson to include concrete type information as a pseudo-property "_class" on serialized JSON
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "_class")
public abstract class BackupTarget implements JobParametersPopulator, HasDescription {

    @Id
    private String id;

    private boolean enabled = true;

    private String name;
}
