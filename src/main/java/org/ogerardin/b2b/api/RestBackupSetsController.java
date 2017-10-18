package org.ogerardin.b2b.api;

import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/backupsets")
public class RestBackupSetsController {

    private final BackupSetRepository backupSetRepository;

    @Autowired
    public RestBackupSetsController(BackupSetRepository backupSetRepository) {
        this.backupSetRepository = backupSetRepository;
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

}
