package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.attribute.BasicFileAttributes;

/**
 * Computes the size of the current backup batch by summing the sizes of files in {@link BackupJobContext#toDoFiles}.
 * The result is stored into {@link BackupJobContext#toDoSize}.
 */
@Slf4j
class ComputeBatchSizeTasklet implements Tasklet {

    private final BackupJobContext context;

    public ComputeBatchSizeTasklet(@NonNull BackupJobContext backupJobContext) {
        this.context = backupJobContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        long batchSize = context.getToDoFiles().stream()
                .map(LocalFileInfo::getFileAttributes)
                .mapToLong(BasicFileAttributes::size)
                .sum();

        log.info("Computed to do size: " + batchSize + " bytes");
        context.setToDoSize(batchSize);

        return RepeatStatus.FINISHED;
    }
}
