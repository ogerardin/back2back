package org.ogerardin.b2b.domain.entity;

import lombok.Data;
import org.ogerardin.b2b.storage.RevisionInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Meta-data about a backed up file.
 */
//TODO maybe merge this with org.ogerardin.b2b.storage.RevisionInfo ??
@Document
@Data
public class StoredFileVersionInfo {

    @Id
    private String path;

    private String md5hash;

    private boolean deleted = false;

    public StoredFileVersionInfo() {
    }

    public StoredFileVersionInfo(String path, String md5hash) {
        this.path = path;
        this.md5hash = md5hash;
    }

    public StoredFileVersionInfo(String path, String md5hash, boolean deleted) {
        this.path = path;
        this.md5hash = md5hash;
        this.deleted = deleted;
    }

    public static StoredFileVersionInfo of(RevisionInfo revisionInfo) {
        return new StoredFileVersionInfo(
                revisionInfo.getFilename(),
                revisionInfo.getMd5hash(),
                revisionInfo.isDeleted()
        );
    }
}
