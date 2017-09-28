package org.ogerardin.b2b.worker;

import lombok.Data;
import org.apache.commons.logging.Log;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.FilesystemSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
public abstract class BackupWorkerBase<T extends BackupTarget> implements Runnable {

    private final FilesystemSource source;
    private final T target;

    protected BackupWorkerBase(FilesystemSource source, T target) {
        this.source = source;
        this.target = target;
    }

    protected void dryRun(Log logger) {
        Path path = Paths.get(source.getPath());

        //noinspection InfiniteLoopStatement
        do {
            try {
                Files.walk(path)
                        .forEach(p -> {
                            logger.info(" FAKE backing up " + p);
                            try {
                                Thread.sleep((long) (Math.random()*10000));
                            } catch (InterruptedException ignored) {}
                        });
            } catch (IOException e) {
                logger.error("Exception while walking directory " + path);
            }
        } while (true);
    }
}
