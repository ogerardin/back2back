package org.ogerardin.b2b.batch.jobs.listeners;

import org.ogerardin.b2b.domain.entity.BackupSet;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
public class InitTaskletExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        //nop
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        // reset stats for current batch
        BackupSet backupSet = getBackupSet();
        backupSet.resetState();
        backupSetRepository.save(backupSet);

        return stepExecution.getExitStatus();
    }
}
