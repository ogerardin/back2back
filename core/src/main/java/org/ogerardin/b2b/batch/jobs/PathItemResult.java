package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.nio.file.Path;

@Data
public class PathItemResult {
    final Path path;
    final BackupResult result;
    final String errorMessage;

    public PathItemResult(Path path, BackupResult result) {
        this.path = path;
        this.result = result;
        this.errorMessage = null;
    }
}
