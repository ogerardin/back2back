package org.ogerardin.b2b.batch.jobs;

import org.ogerardin.b2b.domain.LatestStoredRevisionProvider;
import org.ogerardin.b2b.domain.entity.LatestStoredRevision;
import org.ogerardin.b2b.storage.RevisionInfo;
import org.ogerardin.b2b.storage.StorageFileNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.util.NotImplementedException;

import java.nio.file.Path;
import java.util.Optional;

/**
 * An adapter for {@link StorageService} to implement interface {@link LatestStoredRevisionProvider}.
 */
class StorageServiceLatestStoredRevisionProviderAdapter implements LatestStoredRevisionProvider {
    private final StorageService storageService;

    public StorageServiceLatestStoredRevisionProviderAdapter(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public Optional<LatestStoredRevision> getLatestStoredRevision(String path) {
        try {
            RevisionInfo latestRevision = storageService.getLatestRevision(path);
            LatestStoredRevision revisionInfo = getLatestStoredRevision(latestRevision);
            return Optional.of(revisionInfo);
        } catch (StorageFileNotFoundException e) {
            return Optional.empty();
        }
    }

    private LatestStoredRevision getLatestStoredRevision(RevisionInfo revision) {
        return new LatestStoredRevision(
                revision.getFilename(),
                revision.getMd5hash(),
                revision.isDeleted()
        );
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
    public void saveRevisionInfo(LatestStoredRevision revision) {
        throw new NotImplementedException();
    }

    @Override
    public long deletedCount() {
        return storageService.countDeleted();
    }
}
