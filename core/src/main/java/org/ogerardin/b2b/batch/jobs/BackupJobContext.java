package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the context of a running backup job.
 */
@Data
public class BackupJobContext {

    private final String backupSetId;

    // these fields are populated by the "list files" step of the backup job
    private Set<LocalFileInfo> allFiles = new HashSet<>();
    private long totalSize = 0;

    // these fields are populated by the "filtering" step of the backup job
    private Set<LocalFileInfo> toDoFiles = new HashSet<>();
    private long toDoSize = 0;

    public BackupJobContext(String backupSetId) {
        this.backupSetId = backupSetId;
    }

    public void appendFiles(Set<LocalFileInfo> files) {
        allFiles.addAll(files);
    }

    public void addToTotalSize(long size) {
        totalSize += size;
    }
}
