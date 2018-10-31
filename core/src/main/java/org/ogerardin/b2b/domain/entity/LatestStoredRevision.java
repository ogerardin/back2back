package org.ogerardin.b2b.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Meta-data about a backed up file.
 */
@Document
@Data
public class LatestStoredRevision {

    @Id
    private String path;

    private String md5hash;

    private boolean deleted = false;

    public LatestStoredRevision() {
    }

    public LatestStoredRevision(String path, String md5hash) {
        this.path = path;
        this.md5hash = md5hash;
    }

    public LatestStoredRevision(String path, String md5hash, boolean deleted) {
        this.path = path;
        this.md5hash = md5hash;
        this.deleted = deleted;
    }

}
