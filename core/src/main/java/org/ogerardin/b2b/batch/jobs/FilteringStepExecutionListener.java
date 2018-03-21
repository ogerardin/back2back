package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.util.FormattingHelper;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link StepExecutionListener} that updates the backupSet with the "to do" size from the job context
 * Intended to be attached to the "filtering" step of the backup job.
 */
@Component
@JobScope
@Slf4j
public class FilteringStepExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Autowired
    BackupJobContext backupJobContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Checking for changed files...");
        backupSetRepository.save(backupSet);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        BackupSet backupSet = getBackupSet();

        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            FileSet toDoFiles = backupJobContext.getChangedFiles();
            int fileCount = toDoFiles.fileCount();
            long byteCount = toDoFiles.getByteCount();

            backupSet.setToDoCount(fileCount);
            backupSet.setToDoSize(byteCount);

            backupSet.setBatchCount(fileCount);
            backupSet.setBatchSize(byteCount);

            String status = "To do: " + fileCount + " file(s), " + FormattingHelper.humanReadableByteCount(byteCount);
            log.info(status);
            backupSet.setStatus(status);
        }
        else {
            String status = "Failed to filter files";
            log.error(status);
            backupSet.setStatus(status);
        }
        backupSetRepository.save(backupSet);
        return null; //don't change exit status
    }
}
