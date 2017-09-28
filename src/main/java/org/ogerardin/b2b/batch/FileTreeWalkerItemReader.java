package org.ogerardin.b2b.batch;

import org.ogerardin.b2b.files.RecursivePathCollector;
import org.springframework.batch.item.ItemReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * An {@link ItemReader} that reads all file names recursively under a given directory.
 * Actual tree walking is not performed until the first call to {@link #read()} is made.
 * @see RecursivePathCollector
 */
public class FileTreeWalkerItemReader implements ItemReader<Path> {

    private final RecursivePathCollector pathCollector;

    private Iterator<Path> pathIterator = null;

    public FileTreeWalkerItemReader(Path root) {
        // instantiate the RecursivePathCollector, don't walk the tree yet
        this.pathCollector = new RecursivePathCollector(root);
    }

    @Override
    public Path read() throws IOException {
        if (pathIterator == null) {
            // first call to read, walk the tree and get the iterator
            pathCollector.walkTree();
            pathIterator = pathCollector.getPaths().iterator();
        }
        return pathIterator.hasNext() ? pathIterator.next() : null;
    }

}
