package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Just a container for information about a file. Handy as item type for Spring Batch
 * {@link org.springframework.batch.item.ItemProcessor} and related.
 */
@Data
public class LocalFileInfo {

    private final Path path;
    private final BasicFileAttributes fileAttributes;

    public LocalFileInfo(Path path, BasicFileAttributes fileAttributes) {
        this.path = path;
        this.fileAttributes = fileAttributes;
    }

    public LocalFileInfo(File file) throws IOException {
        this(Paths.get(file.toURI()));
    }

    public LocalFileInfo(Path path) throws IOException {
        this.path = path;
        this.fileAttributes = Files.readAttributes(this.path, BasicFileAttributes.class);
    }
}
