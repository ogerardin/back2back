package org.ogerardin.b2b.api;

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
        return sets.toArray(new BackupSet[sets.size()]);
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

    @GetMapping("/{id}/items")
    public FileVersion[] getItems(@PathVariable String id) {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getAllFileVersions().toArray(FileVersion[]::new);
    }

    @GetMapping("/{id}/items/{itemId}")
    public FileVersion getItemInfo(@PathVariable String id, @PathVariable String itemId) throws StorageFileVersionNotFoundException {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getFileVersion(itemId);
    }

    @GetMapping("/{id}/items/{itemId}/contents")
    @ResponseBody
    public ResponseEntity<Resource> getItemContents(@PathVariable String id, @PathVariable String itemId) throws StorageFileVersionNotFoundException {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        FileVersion fileVersion = storageService.getFileVersion(itemId);
        String filename =Paths.get(fileVersion.getFilename()).getFileName().toString();
        Resource resource = storageService.getFileVersionAsResource(itemId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

}
