package org.ogerardin.b2b.storage;

import lombok.Data;

import java.time.Instant;

@Data
public class StoredFileInfo {

    String id;

    String filename;

    Instant storedDate;

    long size;

    String md5hash;

}
