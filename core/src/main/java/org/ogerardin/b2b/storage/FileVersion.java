package org.ogerardin.b2b.storage;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Provides information about a specific version of a stored file.
 */
@Data
@Builder
public class FileVersion {
    /** The version id (possibly null if the implementation doesn't manage multiple versions) */
    String id;
    /** The original file path */
    String filename;
    /** Time the file was stored */
    Instant storedDate;
    /** Length in bytes of stored file */
    long size;
    /** MD5 hash of the stored file */
    String md5hash;
    /** has the file been deleted since? */
    boolean deleted;
}
