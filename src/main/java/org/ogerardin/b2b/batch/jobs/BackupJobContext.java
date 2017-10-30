package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the context of a backup job, that is mostly the list of files to be backed up.
 */
@Data
public class BackupJobContext {
    private final String backupSetId;

    private Set<Path> allFiles = new HashSet<>();

    public BackupJobContext(String backupSetId) {
        this.backupSetId = backupSetId;
    }
}
