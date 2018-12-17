package org.ogerardin.b2b.domain;

import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemReader;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface of a service that provides meta-information about backed up files and some interaction with it.
 */
public interface FileBackupStatusInfoProvider {

    /**
     * @return the {@link FileBackupStatusInfo} for the latest stored revision of the file corresponding to the
     * specified local path, {@link Optional#empty()} if not found
     */
    Optional<FileBackupStatusInfo> getLatestStoredRevision(String path);

    /**
     * @return the {@link FileBackupStatusInfo} for the latest stored revision of the file corresponding to the
     * specified local path, {@link Optional#empty()} if not found
     */
    default Optional<FileBackupStatusInfo> getLatestStoredRevision(Path path) {
        return getLatestStoredRevision(path.toString());
    }

    /**
     * Marks all known files as "potentially deleted"
     */
    void untouchAll();

    /**
     * save a provided {@link FileBackupStatusInfo}
     */
    void saveStatusInfo(FileBackupStatusInfo revision);

    /**
     * @return an {@link ItemReader} that will read all items where backupRequested is true (in no particular order)
     */
    ItemReader<FileBackupStatusInfo> backupRequestedItemReader();

    /**
     * Remove the entries for deleted files
     * @return the number of deleted entries
     */
    long removeDeleted();
}
