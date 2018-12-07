package org.ogerardin.b2b.storage.filesystem;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConfigurationProperties(prefix = "org.ogerardin.b2b.storage")
@Data
public class FilesystemStorageProperties {

    Path baseDirectory = Paths.get("storage");
}
