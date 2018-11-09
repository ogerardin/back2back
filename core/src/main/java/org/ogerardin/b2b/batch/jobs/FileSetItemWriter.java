package org.ogerardin.b2b.batch.jobs;

import lombok.NonNull;
import org.ogerardin.b2b.batch.jobs.support.FileSet;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * An implementation of {@link ItemWriter} that populates a specified
 * {@link FileSet}
 */
public class FileSetItemWriter implements ItemWriter<LocalFileInfo> {

    private final FileSet fileSet;

    public FileSetItemWriter(@NonNull FileSet fileSet) {
        this.fileSet = fileSet;
        this.fileSet.reset();
    }

    @Override
    public void write(List<? extends LocalFileInfo> items) throws Exception {
        fileSet.add(items);
    }

}
