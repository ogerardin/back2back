package org.ogerardin.b2b.storage.gridfs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("org.ogerardin.b2b.storage.gridfs")
@Data
public class GridFsStorageProperties {

    /**
     * GridFS bucket name
     */
    private String bucket = "gridfs";

}
