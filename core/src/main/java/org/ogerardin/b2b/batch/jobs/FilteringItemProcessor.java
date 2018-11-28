package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.FileSetStats;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.ogerardin.b2b.domain.FileBackupStatusInfoProvider;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Path;
import java.util.function.Predicate;


/**
 * ItemProcessor implementation that filters out (eliminates from further processing) the input item
 * according to a specified filter.
 */
@Data
@Slf4j
class FilteringItemProcessor implements ItemProcessor<LocalFileInfo, LocalFileInfo> {

    private final FileSetStats processedFilesStats = new FileSetStats();
    private final FileSetStats filteredFilesStats = new FileSetStats();

    private final Predicate<Path> filter;
    private final FileBackupStatusInfoProvider fileBackupStatusInfoProvider;

    /**
     * @param filter a {@link Predicate} that filters out files that don't need backup
     */
    public FilteringItemProcessor(FileBackupStatusInfoProvider fileBackupStatusInfoProvider, Predicate<Path> filter) {
        this.filter = filter;
        this.fileBackupStatusInfoProvider = fileBackupStatusInfoProvider;
    }

    @Override
    public LocalFileInfo process(LocalFileInfo item) {

        // update "all files" stats
        processedFilesStats.addFile(item.getFileAttributes().size());

        Path path = item.getPath();

        // mark the file as "not deleted"
        boolean knownFile = fileBackupStatusInfoProvider.touch(path);

        if (! knownFile || filter.test(path)) {
            filteredFilesStats.addFile(item.getFileAttributes().size());
            return item; // item will be passed to the step's writer
        }

        // returning null instructs Spring Batch to NOT pass this item to the step's writer
        return null;
    }

}
