package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.batch.BackupSetAwareBean;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/** An {@link ItemProcessListener} that updates the current BackupSet with progress information */
@Component
@JobScope
public class PathItemProcessListener extends BackupSetAwareBean implements ItemProcessListener<Path, PathItemResult> {

    private static final Log logger = LogFactory.getLog(PathItemProcessListener.class);

    @Override
    public void beforeProcess(Path item) {
//        logger.debug("beforeProcess, backupSet.id=" + getBackupSet().getId());
        BackupSet backupSet = getBackupSet();
        backupSet.setCurrentFile(item);
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterProcess(Path item, PathItemResult result) {
//        logger.debug("afterProcess, backupSet.id=" + getBackupSet().getId());
        BackupSet backupSet = getBackupSet();
        backupSet.setLastFile(item);
        backupSet.setLastFileStatus(result.getResult());
        backupSet.setCurrentFile(null);
        backupSetRepository.save(backupSet);
    }

    @Override
    public void onProcessError(Path item, Exception e) {
        logger.debug("onProcessError, backupSet.id=" + getBackupSet().getId());
        BackupSet backupSet = getBackupSet();
        backupSet.setLastError("Exception while processing " + item + ": " + e.toString());
        backupSetRepository.save(backupSet);
    }
}
