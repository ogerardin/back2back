package org.ogerardin.b2b.worker.peer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.NetworkTarget;
import org.ogerardin.b2b.worker.BackupWorkerBase;

public class PeerBackupWorker extends BackupWorkerBase<NetworkTarget> implements Runnable {

    private static final Log logger = LogFactory.getLog(PeerBackupWorker.class);

    public PeerBackupWorker(FilesystemSource source, NetworkTarget target) {
        super(source, target);
    }

    @Override
    public void run() {
        dryRun(logger);
    }

}
