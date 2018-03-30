package org.ogerardin.b2b.batch.jobs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * ItemProcessor implementation that filters (eliminates from further processing) the input item
 * according to a specified filter.
 */
@Data
@Slf4j
class FilteringPathItemProcessor implements ItemProcessor<LocalFileInfo, LocalFileInfo> {

    private final FileSetStats processedFilesStats = new FileSetStats();
    private final FileSetStats filteredFilesStats = new FileSetStats();

    private final Predicate<Path> filter;

    public FilteringPathItemProcessor(Predicate<Path> filter) {
        this.filter = filter;
    }

    @Override
    public LocalFileInfo process(LocalFileInfo item) throws Exception {

        processedFilesStats.addFile(item.getFileAttributes().size());

        Path path = item.getPath();
        if (filter.test(path)) {
            filteredFilesStats.addFile(item.getFileAttributes().size());
            return item; // item will be passed to step's writer
        }

        // returning null instructs Spring Batch to pass this item to the step's writer
        return null;
    }

}
