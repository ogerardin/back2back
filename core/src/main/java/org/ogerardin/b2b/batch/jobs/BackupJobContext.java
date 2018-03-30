package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

/**
 * Holds the context of a running backup job.
 */
@Data
public class BackupJobContext {

    private final String backupSetId;

    /** The set of files that need to be backep up */
    private final FileSet backupBatch = new FileSet();

}
