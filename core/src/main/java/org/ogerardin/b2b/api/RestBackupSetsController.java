package org.ogerardin.b2b.api;

import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.ogerardin.b2b.storage.StoredFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
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
    public StoredFileInfo[] getItems(@PathVariable String id) {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getAllStoredFileInfos().toArray(StoredFileInfo[]::new);
    }

    @GetMapping("/{id}/items/{itemId}")
    public StoredFileInfo getItemInfo(@PathVariable String id, @PathVariable String itemId) {
        BackupSet backupSet = backupSetRepository.findOne(id);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        return storageService.getStoredFileInfoById(itemId);
    }

}
