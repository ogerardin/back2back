package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class FinalizeBackupTasklet implements Tasklet {

    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;
    private final BackupJobContext jobContext;

    public FinalizeBackupTasklet(FileBackupStatusInfoProvider fileBackupStatusInfoProvider, BackupJobContext jobContext) {
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
        this.jobContext = jobContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.debug("Cleaning up...");

        // remove info about deleted files
        fileBackupStatusInfoProvider.deletedDeleted();

        return RepeatStatus.FINISHED;
    }
}
