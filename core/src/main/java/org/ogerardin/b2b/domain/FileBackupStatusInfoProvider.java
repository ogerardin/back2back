package org.ogerardin.b2b.domain;

import org.ogerardin.b2b.domain.entity.FileBackupStatusInfo;
import org.springframework.batch.item.ItemReader;

import java.nio.file.Path;
import java.util.Map;
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
     * If the specified file is known, mark it as "not deleted", otherwise create an entry.
     * In both cases set {@link FileBackupStatusInfo#currentHashes} to the specified hashes.
     */
    void touch(Path path, Map<String, String> newHashes);

    /**
     * save a provided {@link FileBackupStatusInfo}
     */
    void saveRevisionInfo(FileBackupStatusInfo revision);

    /**
     * @return an {@link ItemReader} that provides all items in no specific order
     */
    ItemReader<FileBackupStatusInfo> reader();

    /**
     * Remove the entries for deleted files
     * @return the number of deleted entries
     */
    long deletedDeleted();
}
