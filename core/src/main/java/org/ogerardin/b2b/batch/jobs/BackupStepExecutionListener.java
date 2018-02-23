package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link StepExecutionListener} that updates the backupSet status
 */
@Component
@JobScope
public class BackupStepExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Autowired
    BackupJobContext backupJobContext;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Backing up");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            BackupSet backupSet = getBackupSet();
            backupSet.setStatus("Backup done");
            backupSetRepository.save(backupSet);
        }
        return null; //don't change exit status
    }
}
