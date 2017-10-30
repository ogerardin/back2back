package org.ogerardin.b2b.batch.jobs;

public enum BackupResult {
    /** File unchanged since last backup */
    UNCHANGED,
    /** File successfully backed up */
    BACKED_UP,
    /** File backup failed */
    ERROR
}
