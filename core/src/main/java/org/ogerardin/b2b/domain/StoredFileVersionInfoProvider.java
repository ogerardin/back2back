package org.ogerardin.b2b.domain;

import org.ogerardin.b2b.storage.FileVersion;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;

import java.nio.file.Path;
import java.util.Optional;

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
     * @return an adapter for the specified {@link StorageService}
     */
    static StoredFileVersionInfoProvider of(StorageService storageService) {
        return path -> {
            try {
                FileVersion latestFileVersion = storageService.getLatestFileVersion(path);
                StoredFileVersionInfo storedFileVersionInfo = StoredFileVersionInfo.of(latestFileVersion);
                return Optional.of(storedFileVersionInfo);
            } catch (StorageFileNotFoundException e) {
                return Optional.empty();
            }
        };
    }
}
