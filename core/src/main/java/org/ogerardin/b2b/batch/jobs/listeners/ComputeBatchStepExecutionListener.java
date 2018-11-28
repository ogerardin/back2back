package org.ogerardin.b2b.batch.jobs.listeners;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.FileSetStats;
import org.ogerardin.b2b.batch.jobs.BackupJobContext;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.entity.BackupSource;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.util.FormattingHelper;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A {@link StepExecutionListener} that updates the backupSet with the total file stats and batch file stats
 * Intended to be attached to the "compute batch" step of the backup job.
 */
@Component
@JobScope
@Slf4j
public class ComputeBatchStepExecutionListener extends BackupSetAwareBean implements StepExecutionListener {

    @Autowired
    private BackupJobContext backupJobContext;

    @Autowired
    private BackupSetStatusPublisher backupSetStatusPublisher;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Collecting and filtering files...");

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String exitCode = stepExecution.getExitStatus().getExitCode();
        BackupSet backupSet = getBackupSet();

        if (exitCode.equals(ExitStatus.COMPLETED.getExitCode())) {
            // total files stats are obtained from the filteringPathItemProcessor
//            updateTotalFilesStats(backupSet, filteringPathItemProcessor.getProcessedFilesStats());
            updateTotalFilesStats(backupSet, backupJobContext.getTotalFileStats());

            // deleted files
//            long deletedCount = filteringPathItemProcessor.getFileBackupStatusInfoProvider().deletedCount();
//            backupJobContext.setDeletedNow(deletedCount);
//            long deletedBefore = backupJobContext.getDeletedBefore();
//            log.info("Deleted file count was {}, now {}", deletedBefore, deletedCount);

            // batch stats are obtained from the current job context
            updateBatchStats(backupSet, backupJobContext.getBackupBatch().getStats());

        }
        else {
            String status = "Failed to compute backup batch";
            log.error(status);
            backupSet.setStatus(status);
        }

        backupSetRepository.save(backupSet);
        backupSetStatusPublisher.publishStatus(backupSet);
        return null; //don't change exit status
    }

    private void updateBatchStats(BackupSet backupSet, FileSetStats batchStats) {
        int fileCount = batchStats.getFileCount();
        long byteCount = batchStats.getByteCount();

        backupSet.setToDoCount(fileCount);
        backupSet.setToDoSize(byteCount);

        backupSet.setBatchCount(fileCount);
        backupSet.setBatchSize(byteCount);

        String status = "To do: " + fileCount + " file(s), " + FormattingHelper.humanReadableByteCount(byteCount);
        log.info(status);
        backupSet.setStatus(status);
    }

    private void updateTotalFilesStats(BackupSet backupSet, FileSetStats allFilesStats) {
        int fileCount = allFilesStats.getFileCount();
        long byteCount = allFilesStats.getByteCount();

        String status = "Found: " + fileCount + " file(s), " + FormattingHelper.humanReadableByteCount(byteCount);
        log.info(status);

        backupSet.setFileCount(fileCount);
        backupSet.setSize(byteCount);
        backupSet.setStatus(status);

        BackupSource backupSource = backupSet.getBackupSource();
        if (backupSource instanceof FilesystemSource) {
            ((FilesystemSource) backupSource).setTotalBytes(byteCount);
            ((FilesystemSource) backupSource).setTotalFiles(fileCount);
        }
    }
}
