package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

/**
 * Holds the context of a running backup job.
 */
@Data
public class BackupJobContext {

    private final String backupSetId;

    // populated by the "list files" step of the backup job
    private final FileSet allFiles = new FileSet();

    // populated by the "filtering" step of the backup job
    private final FileSet changedFiles = new FileSet();

    public BackupJobContext(String backupSetId) {
        this.backupSetId = backupSetId;
    }

    public void reset() {
        allFiles.reset();
        changedFiles.reset();
    }


}
