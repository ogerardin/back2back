package org.ogerardin.b2b.batch.jobs;

import com.google.common.io.Files;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FilesystemItemReader implements ItemReader<LocalFileInfo> {

    private final BackupJobContext context;
    private final Iterator<File> fileIterator;

    public FilesystemItemReader(List<Path> roots, BackupJobContext backupJobContext) {
        this.context = backupJobContext;

        List<File> rootsList = roots.stream().map(Path::toFile).collect(Collectors.toList());

        this.fileIterator = Files.fileTraverser().breadthFirst(rootsList).iterator();
    }

    @Override
    public LocalFileInfo read() throws Exception {
        while (fileIterator.hasNext()) {
            LocalFileInfo localFileInfo = new LocalFileInfo(fileIterator.next());
            if (!localFileInfo.getFileAttributes().isRegularFile()) {
                continue;
            }
            return localFileInfo;
        }
        return null;
    }
}
