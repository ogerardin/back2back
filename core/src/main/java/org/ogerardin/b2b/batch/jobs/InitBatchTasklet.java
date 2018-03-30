package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Data
public class InitBatchTasklet implements Tasklet {

    final BackupJobContext jobContext;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        jobContext.getBackupBatch().reset();
        return RepeatStatus.FINISHED;
    }
}
