package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class PeerFileVersion {

    @Id
    private String path;

    private String md5hash;

    public PeerFileVersion(String path, String md5hash) {
        this.path = path;
        this.md5hash = md5hash;
    }
}
