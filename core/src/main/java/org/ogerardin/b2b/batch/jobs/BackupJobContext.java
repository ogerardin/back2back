package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

/**
 * Holds the context of a running backup job.
 */
@Data
public class BackupJobContext {

    private final String backupSetId;

}
