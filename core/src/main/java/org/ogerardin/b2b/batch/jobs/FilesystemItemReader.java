package org.ogerardin.b2b.batch.jobs;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.jobs.support.LocalFileInfo;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link ItemReader} that provides {@link LocalFileInfo}s by walking the local filesystem from a set of
 * specified root folders.
 */
@Slf4j
public class FilesystemItemReader implements ItemReader<LocalFileInfo> {

    private final Iterator<File> fileIterator;

    public FilesystemItemReader(List<Path> roots) {
        List<File> rootsList = roots.stream().map(Path::toFile).collect(Collectors.toList());
        // We use Guava's Files.fileTraverser() to walk the files because it seems to be the most reliable
        // implementation available, even though it's marked unstable...
        this.fileIterator = Files.fileTraverser().breadthFirst(rootsList).iterator();
    }

    @Override
    public LocalFileInfo read() throws Exception {
        while (fileIterator.hasNext()) {
            File file = fileIterator.next();
            LocalFileInfo localFileInfo;
            try {
                localFileInfo = new LocalFileInfo(file);
                //ignore files other than regular files
                //TODO how to handle symbolic links ?
                if (!localFileInfo.getFileAttributes().isRegularFile()) {
                    continue;
                }
            } catch (IOException e) {
                if (log.isDebugEnabled()) {
                    log.error("Failed to read file information for " + file, e);
                } else {
                    log.error("Failed to read file information for {}: {}", file, e.toString());
                }
                continue;
            }
            return localFileInfo;
        }
        // no more files
        return null;
    }
}
