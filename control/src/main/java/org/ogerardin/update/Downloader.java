package org.ogerardin.update;

import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

@Data
public class Downloader {

    public Path downloadFile(URL url, Path dir) throws IOException {

        String[] parts = url.getPath().split("/");
        String filename = parts[parts.length - 1];

        Path targetFile = dir.resolve(filename);

        FileUtils.copyURLToFile(url, targetFile.toFile(), 10000, 10000);

        return targetFile;
    }
}
