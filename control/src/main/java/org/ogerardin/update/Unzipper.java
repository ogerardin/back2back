package org.ogerardin.update;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Unzipper {

    public void unzip(Path zipFile, Path outputFolder) throws IOException {
        ZipFile zf = new ZipFile(zipFile.toFile());
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String fileName = entry.getName();
            if (entry.getSize() == 0 || fileName.endsWith("/")) {
                continue;
            }
            Path extractFile = outputFolder.resolve(fileName);
            Files.createDirectories(extractFile.getParent());
            try (
                    InputStream inputStream = new BufferedInputStream(zf.getInputStream(entry))
            ) {
                FileUtils.copyInputStreamToFile(inputStream, extractFile.toFile());
            }
        }
    }


}
