package org.ogerardin.b2b.storage;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Provides information about a specific revision of a file.
 */
@Data
@Builder
public class RevisionInfo {
    /** The revision id (possibly null if the implementation doesn't manage multiple revisions) */
    String id;

    /** The original file path */
    String filename;

    /** Time the file was stored */
    Instant storedDate;

    /** Length in bytes of stored file */
    long size;

    /** MD5 hash of the original (unencrypted) file */
    String md5hash;

    /** has the file been deleted since? */
    boolean deleted;
}
