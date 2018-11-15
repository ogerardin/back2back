package org.ogerardin.b2b.domain;

import org.ogerardin.b2b.domain.entity.LatestStoredRevision;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Interface of a service that provides meta-information about backed up files and some interaction with it.
 */
public interface LatestStoredRevisionProvider {
    //TODO the mechanism for marking deleted files (#untouchAll/#touch) is not very efficient
    // and should be replaced with a better one

    /**
     * @return the {@link LatestStoredRevision} for the latest stored revision of the file corresponding to the
     * specified local path, {@link Optional#empty()} if not found
     */
    Optional<LatestStoredRevision> getLatestStoredRevision(String path);

    /**
     * @return the {@link LatestStoredRevision} for the latest stored revision of the file corresponding to the
     * specified local path, {@link Optional#empty()} if not found
     */
    default Optional<LatestStoredRevision> getLatestStoredRevision(Path path) {
        return getLatestStoredRevision(path.toString());
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
     * save a provided {@link LatestStoredRevision}
     */
    void saveRevisionInfo(LatestStoredRevision revision);

//    long deletedCount();

}
