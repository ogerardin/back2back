package org.ogerardin.b2b.storage.filesystem;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("org/ogerardin/b2b/storage/filesystem")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String location = "upload-dir";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
