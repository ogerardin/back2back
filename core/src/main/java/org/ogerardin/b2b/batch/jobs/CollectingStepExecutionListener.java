package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.util.FormattingHelper;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link StepExecutionListener} that updates the backupSet with the file count from the job context.
 * Intended to be attached to the "collect files" step of the backup job.
 */
@Component
@JobScope
@Slf4j
public class CollectingStepExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Autowired
    BackupJobContext backupJobContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Collecting files...");
        backupSetRepository.save(backupSet);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        BackupSet backupSet = getBackupSet();

        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            FileSet allFiles = backupJobContext.getAllFiles();
            int fileCount = allFiles.fileCount();
            long byteCount = allFiles.getByteCount();
            String status = "Collected files: " + fileCount + " file(s), " + FormattingHelper.humanReadableByteCount(byteCount);
            log.info(status);

            backupSet.setFileCount(fileCount);
            backupSet.setSize(byteCount);

            BackupSource backupSource = backupSet.getBackupSource();
            if (backupSource != null) {
                ((FilesystemSource) backupSource).setTotalBytes(byteCount);
                ((FilesystemSource) backupSource).setTotalFiles(fileCount);
            }

            backupSet.setStatus(status);
        }
        else {
            String status = "Failed to collect files";
            log.error(status);
            backupSet.setStatus(status);
        }
        backupSetRepository.save(backupSet);
        return null; //don't change exit status
    }
}
