package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.BackupSetAwareBean;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link StepExecutionListener} that updates the backupSet with the file count from the job context.
 * Intended to be attached to the {@link ListFilesTasklet} step.
 */
@Component
@JobScope
public class ListFilesTaskletExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Autowired
    BackupJobContext backupJobContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Collecting files");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            int fileCount = backupJobContext.getChangedFiles().size();
            long totalSize = backupJobContext.getTotalSize();
            BackupSet backupSet = getBackupSet();
            backupSet.setStatus("Collected " + fileCount + " files");
            backupSet.setFileCount(fileCount);
            backupSet.setSize(totalSize);
            backupSetRepository.save(backupSet);
        }
        return null; //don't change exit status
    }
}
