package org.ogerardin.b2b.worker.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.worker.BackupWorkerBase;
import org.ogerardin.b2b.worker.SingleFileProcessor;

import java.io.File;

public class LocalBackupWorker extends BackupWorkerBase<LocalTarget> implements Runnable {

    private static final Log logger = LogFactory.getLog(LocalBackupWorker.class);

    public LocalBackupWorker(FilesystemSource source, LocalTarget target) {
        super(source, target);
    }

    @Override
    public void run() {
        dryRun(logger);
    }

    public static class FileProcessor implements SingleFileProcessor {
        @Override
        public void process(File f) {
            logger.info(" FAKE processing " + f);
        }
    }

}
