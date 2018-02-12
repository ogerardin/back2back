package org.ogerardin.b2b.domain;

import org.springframework.data.annotation.Transient;

/**
 * Provides a non-serialized "description" field.
 */
public interface HasDescription {

    @Transient
    String getDescription();
}
