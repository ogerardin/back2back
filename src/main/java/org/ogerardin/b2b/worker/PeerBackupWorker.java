package org.ogerardin.b2b.worker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.NetworkTarget;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PeerBackupWorker extends BackupWorker implements Runnable {

    private static final Log logger = LogFactory.getLog(PeerBackupWorker.class);


    private final FilesystemSource source;
    private final NetworkTarget target;

    public PeerBackupWorker(FilesystemSource source, NetworkTarget target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void run() {
        Path path = Paths.get(source.getPath());

        try {
            Files.walk(path)
                    .forEach(p -> {
                        System.out.println(" FAKE backing up " + p);
                    });
        } catch (IOException e) {
            logger.error("Exception while walking directory " + path);
        }

    }
}
