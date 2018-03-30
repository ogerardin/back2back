package org.ogerardin.b2b.batch.jobs;

import lombok.Data;

/**
 * Statistics about a set of files.
 */
@Data
public class FileSetStats {

    private int fileCount = 0;
    private long byteCount = 0;

    public FileSetStats(int fileCount, long byteCount) {
        this.fileCount = fileCount;
        this.byteCount = byteCount;
    }

    public FileSetStats() {
    }

    public void addFile(long size) {
        fileCount++;
        byteCount += size;
    }
}
