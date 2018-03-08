package org.ogerardin.b2b.batch.jobs;

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
public class FileBackupListener extends BackupSetAwareBean implements ItemWriteListener<LocalFileInfo> {

    @Override
    public void beforeWrite(List<? extends LocalFileInfo> items) {
        Path[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Backing up " + Arrays.toString(paths));
        backupSetRepository.save(backupSet);
    }

    @Override
    public void afterWrite(List<? extends LocalFileInfo> items) {
        Path[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("Finished backing up " + Arrays.toString(paths));
        // subtract written size and count from to do
        long writtenSize = items.stream()
                .map(LocalFileInfo::getFileAttributes)
                .mapToLong(BasicFileAttributes::size)
                .sum();

        backupSet.setToDoSize(backupSet.getToDoSize() - writtenSize);
        backupSet.setToDoCount(backupSet.getToDoCount() - items.size());
        backupSetRepository.save(backupSet);
    }

    @Override
    public void onWriteError(Exception exception, List<? extends LocalFileInfo> items) {
        Path[] paths = getPaths(items);
        BackupSet backupSet = getBackupSet();
        backupSet.setStatus("ERROR backing up " + Arrays.toString(paths));
        backupSetRepository.save(backupSet);

    }

    private Path[] getPaths(List<? extends LocalFileInfo> items) {
        return items.stream()
                .map(LocalFileInfo::getPath)
                .toArray(Path[]::new);
    }
}
