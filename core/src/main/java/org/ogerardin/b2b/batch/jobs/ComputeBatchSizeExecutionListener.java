package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link StepExecutionListener} that updates the backupSet with the "to do" size from the job context
 * Intended to be attached to the {@link ComputeBatchSizeTasklet} step.
 */
@Component
@JobScope
public class ComputeBatchSizeExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Autowired
    BackupJobContext backupJobContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Computing batch size");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        BackupSet backupSet = getBackupSet();
        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            long toDoSize = backupJobContext.getToDoSize();
            int toDoCount = backupJobContext.getToDoFiles().size();
            backupSet.setStatus("To do: " + toDoCount + " files, " + toDoSize + " bytes");
            backupSet.setToDoCount(toDoCount);
            backupSet.setToDoSize(toDoSize);
        }
        else {
            backupSet.setStatus("Failed to compute batch size");
        }
        backupSetRepository.save(backupSet);
        return null; //don't change exit status
    }
}
