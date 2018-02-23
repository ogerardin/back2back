package org.ogerardin.b2b.api.rest;

import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.storage.StorageFileVersionNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.ogerardin.b2b.storage.FileVersion;
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
public class RestBackupSetsController {

    private final BackupSetRepository backupSetRepository;

    private final StorageServiceFactory storageServiceFactory;

    @Autowired
    public RestBackupSetsController(BackupSetRepository backupSetRepository, @Qualifier("gridFsStorageServiceFactory") StorageServiceFactory storageServiceFactory) {
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
        return backupSetRepository.findOne(id);
    }

    @GetMapping("/{id}/files")
    public Path[] getFiles(@PathVariable String id) {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getAllPaths().toArray(Path[]::new);
    }

    /**
     * @param path the path of the file to query; must be url-encoded. We use a request parameter rather than a
     *             path parameter because it's more difficult to pass slashes inside a path parameter.
     */
    @GetMapping("/{id}/versions")
    public FileVersion[] getVersions(@PathVariable String id, @RequestParam(required = false) String path) {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        if (path == null) {
            return storageService.getAllFileVersions().toArray(FileVersion[]::new);
        }
        else {
            return storageService.getFileVersions(path);
        }
    }

    @GetMapping("/{id}/versions/{versionId}")
    public FileVersion getItemInfo(@PathVariable String id, @PathVariable String versionId) throws StorageFileVersionNotFoundException {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getFileVersion(versionId);
    }

    @GetMapping("/{id}/versions/{versionId}/contents")
    @ResponseBody
    public ResponseEntity<Resource> getItemContents(@PathVariable String id, @PathVariable String versionId) throws StorageFileVersionNotFoundException {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        FileVersion fileVersion = storageService.getFileVersion(versionId);
        String filename =Paths.get(fileVersion.getFilename()).getFileName().toString();
        Resource resource = storageService.getFileVersionAsResource(versionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

}
