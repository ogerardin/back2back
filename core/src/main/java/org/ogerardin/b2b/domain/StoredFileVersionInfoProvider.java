package org.ogerardin.b2b.domain;

import org.ogerardin.b2b.domain.entity.StoredFileVersionInfo;
import org.ogerardin.b2b.storage.FileVersion;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.util.NotImplementedException;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface of a service that provides meta-information about backed up files and some interaction with it.
 */
public interface StoredFileVersionInfoProvider {

    /**
     * @return the {@link StoredFileVersionInfo} for the latest stored version of the file corresponding to the
     * specified local path, {@link Optional#empty()} if not found
     */
    Optional<StoredFileVersionInfo> getStoredFileVersionInfo(String path);

    /**
     * @return the {@link StoredFileVersionInfo} for the latest stored version of the file corresponding to the
     * specified local path, {@link Optional#empty()} if not found
     */
    default Optional<StoredFileVersionInfo> getStoredFileVersionInfo(Path path) {
        return getStoredFileVersionInfo(path.toString());
    }

    /**
     * Marks all known files as "deleted"
     */
    void untouchAll();

    /**
     * If the specified file is known, mark it as "not deleted"; otherwise do nothing.
     * @return true if the file was known, false otherwise
     */
    boolean touch(Path path);

    /**
     * save a provided {@link StoredFileVersionInfo}
     */
    void saveStoredFileVersionInfo(StoredFileVersionInfo storedFileVersionInfo);

    long deletedCount();

    /**
     * @return an adapter for the specified {@link StorageService}
     */
    //FIXME should not be here
    static StoredFileVersionInfoProvider of(StorageService storageService) {
        return new StoredFileVersionInfoProvider() {
            @Override
            public Optional<StoredFileVersionInfo> getStoredFileVersionInfo(String path) {
                try {
                    FileVersion latestFileVersion = storageService.getLatestFileVersion(path);
                    StoredFileVersionInfo storedFileVersionInfo = StoredFileVersionInfo.of(latestFileVersion);
                    return Optional.of(storedFileVersionInfo);
                } catch (StorageFileNotFoundException e) {
                    return Optional.empty();
                }
            }

            @Override
            public void untouchAll() {
                storageService.untouchAll();
            }

            @Override
            public boolean touch(Path path) {
                return storageService.touch(path);
            }

            @Override
            public void saveStoredFileVersionInfo(StoredFileVersionInfo storedFileVersionInfo) {
                throw new NotImplementedException();
            }

            @Override
            public long deletedCount() {
                return storageService.countDeleted();
            }
        };
    }
}
