package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A set of {@link LocalFileInfo}, also maintaining a count of total bytes.
 */
@Data
public class FileSet {

    private final Set<LocalFileInfo> files = new HashSet<>();
    private long byteCount = 0;

    /**
     * Adds the specified files and updates the total byte count
     * @param files
     */
    public void add(Collection<? extends LocalFileInfo> files) {
        this.files.addAll(files);
        long byteCount = files.stream()
                .map(LocalFileInfo::getFileAttributes)
                .mapToLong(BasicFileAttributes::size)
                .sum();
        this.byteCount += byteCount;
    }

    public int getFileCount() {
        return files.size();
    }

    public void reset() {
        this.files.clear();
        this.byteCount = 0;
    }

    FileSetStats getStats() {
        return new FileSetStats(getFileCount(), getByteCount());
    }
}
