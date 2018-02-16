package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@Data
public class FileInfo {

    private final Path path;

    private final BasicFileAttributes fileAttributes;
}
