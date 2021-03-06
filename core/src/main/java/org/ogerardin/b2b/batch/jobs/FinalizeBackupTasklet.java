package org.ogerardin.b2b.batch.jobs;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

import java.util.Arrays;

@Slf4j
public class FinalizeBackupTasklet implements Tasklet {

    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    public FinalizeBackupTasklet(FileBackupStatusInfoProvider fileBackupStatusInfoProvider) {
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        log.info("Cleaning up...");

        // remove info about deleted files
        if (log.isDebugEnabled()) {
            log.debug("Deleted files: {}", Arrays.toString(fileBackupStatusInfoProvider.deletedFiles()));
        }
        long deletedCount = fileBackupStatusInfoProvider.removeDeleted();
        log.info("Removed {} entries for deleted files", deletedCount);

        return RepeatStatus.FINISHED;
    }
}
