package org.ogerardin.b2b.storage.gridfs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties("org.ogerardin.b2b.storage.gridfs")
@Data
public class GridFsStorageProperties {

    /**
     * GridFS bucket name
     */
    private String bucket = "b2b-bucket";

}
