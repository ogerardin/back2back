package org.ogerardin.b2b.batch.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import javax.validation.constraints.NotNull;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Computes the size of the current backup batch by summing the sizes of files in {@link BackupJobContext#toDoFiles}.
 * The result is stored into {@link BackupJobContext#toDoSize}.
 */
class ComputeBatchSizeTasklet implements Tasklet {
    private static final Log logger = LogFactory.getLog(ComputeBatchSizeTasklet.class);

    private final BackupJobContext context;

    public ComputeBatchSizeTasklet(@NotNull BackupJobContext backupJobContext) {
        this.context = backupJobContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        long batchSize = context.getToDoFiles().stream()
                .map(FileInfo::getFileAttributes)
                .mapToLong(BasicFileAttributes::size)
                .sum();

        logger.info("Batch size is: " + batchSize);
        context.setToDoSize(batchSize);

        return RepeatStatus.FINISHED;
    }
}
