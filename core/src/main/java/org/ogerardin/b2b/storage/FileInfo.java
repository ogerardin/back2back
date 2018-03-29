package org.ogerardin.b2b.storage;

import lombok.Data;

import java.nio.file.Path;

@Data
public class FileInfo {
    final Path path;
    final boolean deleted;
}
