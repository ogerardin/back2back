package org.ogerardin.b2b.batch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@JobScope
public class PathItemProcessListener extends BackupSetAwareListener implements ItemProcessListener<Path, Path> {

    private static final Log logger = LogFactory.getLog(PathItemProcessListener.class);

    @Override
    public void beforeProcess(Path item) {
        logger.debug("beforeProcess, backupSet.id=" + getBackupSet().getId());
    }

    @Override
    public void afterProcess(Path item, Path result) {

    }

    @Override
    public void onProcessError(Path item, Exception e) {

    }
}
