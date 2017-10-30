package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.nio.file.Path;

@Data
public class PathItemResult {
    final Path path;
    final BackupResult result;
}
