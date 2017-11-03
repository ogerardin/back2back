package org.ogerardin.b2b.storage.filesystem;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "org.ogerardin.b2b")
@Data
public class FilesystemStorageProperties {

    Path baseDirectory = Paths.get("storage");
}
