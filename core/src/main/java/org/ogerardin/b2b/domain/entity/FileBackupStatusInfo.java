package org.ogerardin.b2b.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Meta-data about a backed up file.
 */
@Document
@Data
public class FileBackupStatusInfo {

    @Id
    private String path;

    @Deprecated
    private String md5hash;

    private Map<String, String> hashes = new HashMap<>();

    private boolean deleted = false;

    public FileBackupStatusInfo() {
    }

    @Deprecated
    public FileBackupStatusInfo(String path, String md5hash) {
        this.path = path;
        this.md5hash = md5hash;
        this.hashes.put("MD5", md5hash);
    }

    @Deprecated
    public FileBackupStatusInfo(String path, String md5hash, boolean deleted) {
        this.path = path;
        this.md5hash = md5hash;
        this.deleted = deleted;
        this.hashes.put("MD5", md5hash);
    }

    public FileBackupStatusInfo setHash(String hashName, String hashValue) {
        hashes.put(hashName, hashValue);
        return this;
    }

}
