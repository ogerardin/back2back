package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Just a container for information about a file. Handy as item type for Spring Batch
 * {@link org.springframework.batch.item.ItemProcessor} and related.
 */
@Data
public class LocalFileInfo {

    private final Path path;

    private final BasicFileAttributes fileAttributes;
}
