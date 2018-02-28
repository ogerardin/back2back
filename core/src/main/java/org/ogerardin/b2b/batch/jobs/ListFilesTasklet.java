package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.files.RecursivePathCollector;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tasklet that populates the provided job context with the list of files to be backed up, so the item reader can
 * just iterate over this list. This effectively decouples the directory walking from the backup itself.
 */
class ListFilesTasklet implements Tasklet {
    private final Path root;
    private final BackupJobContext context;

    public ListFilesTasklet(String root, BackupJobContext backupJobContext) {
        this.root = Paths.get(root);
        this.context = backupJobContext;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        //walk the root directory
        RecursivePathCollector pathCollector = new RecursivePathCollector(root);
        pathCollector.walkTree();

        //put results in context
        Set<LocalFileInfo> allFiles = pathCollector.getPaths().entrySet().stream()
                .map(e -> new LocalFileInfo(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        context.setAllFiles(allFiles);
        context.setTotalSize(pathCollector.getSize());

        return RepeatStatus.FINISHED;
    }
}
