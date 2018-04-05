package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.StoredFileVersionInfoProvider;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Data
@Slf4j
public class InitBatchTasklet implements Tasklet  {

    final StoredFileVersionInfoProvider storedFileVersionInfoProvider;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("Setting deleted flag");
        // Set the "deleted" flag to true for all stored files.
        // Later as we examine each file, we will set this flag to false, whether the file is backed up or not.
        // At the end only the files that do not exist anymore in the source will still have their deleted flag set to true.
        storedFileVersionInfoProvider.untouchAll();

        return RepeatStatus.FINISHED;
    }
}
