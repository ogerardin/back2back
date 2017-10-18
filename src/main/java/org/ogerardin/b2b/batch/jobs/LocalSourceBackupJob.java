package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.FilesystemSource;

/**
 * Abstract superclass for jobs that accept a source of type {@link FilesystemSource}
 */
public abstract class LocalSourceBackupJob extends BackupJob {

    public LocalSourceBackupJob() {
        addStaticParameter("source.type", FilesystemSource.class.getName());
        addMandatoryParameter("source.root");
    }

}
