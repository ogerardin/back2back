package org.ogerardin.b2b.api;

import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/backupsets")
public class BackupSetsController {

    private final BackupSetRepository backupSetRepository;

    private final StorageServiceFactory storageServiceFactory;

    @Autowired
    public BackupSetsController(BackupSetRepository backupSetRepository, @Qualifier("gridFsStorageServiceFactory") StorageServiceFactory storageServiceFactory) {
        this.backupSetRepository = backupSetRepository;
        this.storageServiceFactory = storageServiceFactory;
    }

    @GetMapping
    // we return an array (and not a List) to make item type accessible to JSON serialization
    public BackupSet[] getAll() {
        List<BackupSet> sets = backupSetRepository.findAll();
        return sets.toArray(new BackupSet[0]);
    }

    @GetMapping("/{id}")
    public BackupSet get(@PathVariable String id) {
        return backupSetRepository.findById(id).get();
    }

    /**
     * Returns all the {@link Path}s stored as part of the {@link BackupSet} with the specified ID.
     * @param includeDeleted is specified and true, also include the files that do not longer exist in the source.
     */
    @GetMapping("/{id}/files")
    public FileInfo[] getFiles(@PathVariable String id, @RequestParam(required = false) Boolean includeDeleted) {
        if (includeDeleted == null) {
            includeDeleted = false;
        }
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getAllFiles(includeDeleted).toArray(FileInfo[]::new);
    }

    /**
     * @param path the path of the file to query; must be url-encoded. We use a request parameter rather than a
     *             path variable because it's awkward to handle slashes inside a path variable.
     */
    @GetMapping("/{id}/revisions")
    public RevisionInfo[] getVersions(@PathVariable String id, @RequestParam(required = false) String path) {
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        if (path == null) {
            return storageService.getAllRevisions().toArray(RevisionInfo[]::new);
        }
        else {
            return storageService.getRevisions(path);
        }
    }

    @GetMapping("/{id}/revisions/{versionId}")
    public RevisionInfo getItemInfo(@PathVariable String id, @PathVariable String versionId) throws StorageFileVersionNotFoundException {
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getRevisionInfo(versionId);
    }

    @GetMapping("/{id}/revisions/{versionId}/contents")
    @ResponseBody
    public ResponseEntity<Resource> getItemContents(@PathVariable String id, @PathVariable String versionId) throws StorageFileVersionNotFoundException {
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        RevisionInfo revisionInfo = storageService.getRevisionInfo(versionId);
        String filename = Paths.get(revisionInfo.getFilename()).getFileName().toString();
        Resource resource = storageService.getRevisionAsResource(versionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

}
