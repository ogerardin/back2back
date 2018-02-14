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
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            long fileCount = backupJobContext.getAllFiles().size();
            BackupSet backupSet = getBackupSet();
            backupSet.setFileCount(fileCount);
            backupSetRepository.save(backupSet);
        }
        return null; //don't change exit status
    }
}
