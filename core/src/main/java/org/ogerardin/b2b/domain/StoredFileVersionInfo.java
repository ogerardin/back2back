package org.ogerardin.b2b.domain;

import lombok.Data;
import org.ogerardin.b2b.storage.FileVersion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Meta-data about a backed up file.
 */
//TODO maybe merge this with org.ogerardin.b2b.storage.FileVersion ??
@Document
@Data
public class StoredFileVersionInfo {

    @Id
    private String path;

    private String md5hash;

    private boolean deleted = false;

    public StoredFileVersionInfo(String path, String md5hash) {
        this.path = path;
        this.md5hash = md5hash;
    }

    public StoredFileVersionInfo(String path, String md5hash, boolean deleted) {
        this.path = path;
        this.md5hash = md5hash;
        this.deleted = deleted;
    }

    public static StoredFileVersionInfo of(FileVersion fileVersion) {
        return new StoredFileVersionInfo(
                fileVersion.getFilename(),
                fileVersion.getMd5hash(),
                fileVersion.isDeleted()
        );
    }
}
