package org.ogerardin.b2b.batch.jobs;

import com.google.common.io.Files;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link ItemReader} that provides {@link LocalFileInfo}s by walking the local filesystem from a set of
 * specified root folders.
 */
public class FilesystemItemReader implements ItemReader<LocalFileInfo> {

    private final Iterator<File> fileIterator;

    public FilesystemItemReader(List<Path> roots) {
        List<File> rootsList = roots.stream().map(Path::toFile).collect(Collectors.toList());
        // We use Guava's Files.fileTraverser() to walk the files
        this.fileIterator = Files.fileTraverser().breadthFirst(rootsList).iterator();
    }

    @Override
    public LocalFileInfo read() throws Exception {
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            LocalFileInfo localFileInfo = new LocalFileInfo(file);
            //ignore files other than regular files
            if (!localFileInfo.getFileAttributes().isRegularFile()) {
                continue;
            }
            return localFileInfo;
        }
        // no more files
        return null;
    }
}
