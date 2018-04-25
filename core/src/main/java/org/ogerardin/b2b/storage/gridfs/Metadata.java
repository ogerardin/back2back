package org.ogerardin.b2b.storage.gridfs;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Metadata information about a stored file. This is intended to be stored in GridFS' metadata field as serialized JSON
 */
@Data
@JsonSerialize
public class Metadata {

    /** Was the original file deleted after this backup? */
    private boolean deleted = false;

    /** Is the stored file encrypted? */
    private boolean encrypted = false;

    /** MD5 hash of the original (unencrypted) file */
    private String md5hash;
}
