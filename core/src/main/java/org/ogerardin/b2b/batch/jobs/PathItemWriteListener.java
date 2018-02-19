package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.batch.BackupSetAwareBean;
import org.ogerardin.b2b.domain.BackupSet;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

@Component
@JobScope
public class PathItemWriteListener extends BackupSetAwareBean implements ItemWriteListener<FileInfo> {

    @Override
    public void beforeWrite(List<? extends FileInfo> items) {
        Path[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Backing up " + Arrays.toString(paths));
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterWrite(List<? extends FileInfo> items) {
        Path[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Finished backing up " + Arrays.toString(paths));
        // subtract written size and count from to do
        long writtenSize = items.stream()
                .map(FileInfo::getFileAttributes)
                .mapToLong(BasicFileAttributes::size)
                .sum();
        backupSet.setToDoSize(backupSet.getToDoSize() - writtenSize);
        backupSet.setToDoCount(backupSet.getToDoCount() - items.size());
        backupSetRepository.save(backupSet);
    }

    @Override
    public void onWriteError(Exception exception, List<? extends FileInfo> items) {
        Path[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("ERROR backing up " + Arrays.toString(paths));
        backupSetRepository.save(backupSet);

    }

    private Path[] getPaths(List<? extends FileInfo> items) {
        return items.stream()
                .map(FileInfo::getPath)
                .toArray(Path[]::new);
    }
}
