package org.ogerardin.b2b.worker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.LocalTarget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalBackupWorker extends BackupWorkerBase<LocalTarget> implements Runnable {

    private static final Log logger = LogFactory.getLog(LocalBackupWorker.class);

    public LocalBackupWorker(FilesystemSource source, LocalTarget target) {
        super(source, target);
    }

    @Override
    public void run() {
        dryRun(logger);

    }

}
