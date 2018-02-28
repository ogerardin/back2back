package org.ogerardin.b2b.domain;

import lombok.Data;
import org.ogerardin.b2b.storage.FileVersion;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//TODO maybe merge this with org.ogerardin.b2b.storage.FileVersion ??
@Document
@Data
public class StoredFileVersionInfo {

    @Id
    private String path;

    private String md5hash;

    public StoredFileVersionInfo(String path, String md5hash) {
        this.path = path;
        this.md5hash = md5hash;
    }

    public static StoredFileVersionInfo of(FileVersion fileVersion) {
        return new StoredFileVersionInfo(fileVersion.getFilename(), fileVersion.getMd5hash());
    }
}
