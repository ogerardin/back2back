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

    // these fields are populated by the ListFilesTasklet (step 1)
    private Set<FileInfo> allFiles = new HashSet<>();
    private long totalSize = 0;

    // this field is populated by the ItemlWriter of step 2
    private Set<FileInfo> toDoFiles = new HashSet<>();
    private long toDoSize = 0;

    public BackupJobContext(String backupSetId) {
        this.backupSetId = backupSetId;
    }

}
