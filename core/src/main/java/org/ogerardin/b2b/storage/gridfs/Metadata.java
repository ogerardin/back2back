package org.ogerardin.b2b.storage.gridfs;

import lombok.Data;

/**
 * Metadata information about a stored file. This is intended to be stored as GridFS metadata.
 */
@Data
public class Metadata {

    /** Was the original file deleted after this backup? */
    private boolean deleted = false;

    /** Is the stored file encrypted? */
    private boolean encrypted = false;

    /** MD5 hash of the unencrypted file */
    private String md5hash;
}
