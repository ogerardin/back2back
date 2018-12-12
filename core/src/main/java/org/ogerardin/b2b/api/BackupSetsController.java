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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

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
        includeDeleted = Optional.ofNullable(includeDeleted).orElse(Boolean.FALSE);
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = getStorageService(backupSet);
        return storageService.getAllFiles(includeDeleted).toArray(FileInfo[]::new);
    }

    /**
     * Returns a {@link StorageService} that is specific to the specified {@link BackupSet}.
     */
    private StorageService getStorageService(BackupSet backupSet) {
        // We use the backup set ID as storage service name. How this will translate in terms of storage is
        // dependent on the specific StorageService implementation; for example for GridFsStorageService
        // it will be used as the GridFS bucket name.
        return storageServiceFactory.getStorageService(backupSet.getId());
        // TODO we should implement a maintenance job to delete StorageService resources for which there is no
        //  more corresponding backupSet
    }

    /**
     * @param path the path of the file to query; must be url-encoded. We use a request parameter rather than a
     *             path variable because it's awkward to handle slashes inside a path variable.
     */
    @GetMapping("/{id}/revisions")
    public RevisionInfo[] getVersions(@PathVariable String id, @RequestParam(required = false) String path) {
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = getStorageService(backupSet);
        if (path == null) {
            return storageService.getAllRevisions().toArray(RevisionInfo[]::new);
        }
        else {
            return storageService.getRevisions(path);
        }
    }

    @GetMapping("/{id}/revisions/{revisionId}")
    public RevisionInfo getItemInfo(@PathVariable String id, @PathVariable String revisionId) throws RevisionNotFoundException {
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = getStorageService(backupSet);
        return storageService.getRevisionInfo(revisionId);
    }

    @GetMapping("/{id}/revisions/{revisionId}/contents")
    @ResponseBody
    public ResponseEntity<Resource> getItemContents(@PathVariable String id, @PathVariable String revisionId) throws RevisionNotFoundException, IOException {
        BackupSet backupSet = backupSetRepository.findById(id).get();
        StorageService storageService = getStorageService(backupSet);
        RevisionInfo revisionInfo = storageService.getRevisionInfo(revisionId);
        String filename = Paths.get(revisionInfo.getFilename()).getFileName().toString();
        Resource resource = storageService.getRevisionAsResource(revisionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

}
