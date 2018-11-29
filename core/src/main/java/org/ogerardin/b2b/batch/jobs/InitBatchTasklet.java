package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

@Data
@Slf4j
public class InitBatchTasklet implements Tasklet  {

    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    private final BackupJobContext backupJobContext;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {

//        long deletedCount = fileBackupStatusInfoProvider.deletedCount();
//        backupJobContext.setDeletedBefore(deletedCount);

        log.debug("Setting deleted flag");
        // Set the "deleted" flag to true for all stored files.
        // Later as we examine each existing file, we will set this flag to false (whether the file needs to be backed up or not).
        // At the end only the files that do not exist anymore in the source will still have their deleted flag set to true.
        fileBackupStatusInfoProvider.untouchAll();

        return RepeatStatus.FINISHED;
    }
}
