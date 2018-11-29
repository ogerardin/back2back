package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.FileSetStats;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;

import java.util.function.Predicate;


/**
 * ItemProcessor implementation that filters out (eliminates from further processing) the input item
 * according to a given {@link Predicate}
 */
@Data
@Slf4j
class FilteringItemProcessor implements ItemProcessor<LocalFileInfo, LocalFileInfo> {

    private final FileSetStats processedFilesStats = new FileSetStats();
    private final FileSetStats filteredFilesStats = new FileSetStats();

    private final Predicate<LocalFileInfo> filter;

    /**
     * @param filter a {@link Predicate} that should return true if the file must be backed up, false otherwise.
     */
    public FilteringItemProcessor(Predicate<LocalFileInfo> filter) {
        this.filter = filter;
    }

    @Override
    public LocalFileInfo process(@NonNull LocalFileInfo item) {

        // update "all files" stats
        processedFilesStats.addFile(item.getFileAttributes().size());

        if (filter.test(item)) {
            // file must be backed up!

            // update "seleted for backup" stats
            filteredFilesStats.addFile(item.getFileAttributes().size());

            return item; // item will be passed to the step's writer
        }

        // no need to backup
        // return null to instruct Spring Batch to NOT pass this item to the step's writer
        return null;
    }

}
