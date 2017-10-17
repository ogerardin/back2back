package org.ogerardin.b2b.api;

import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class RestBackupSourcesController {

    private final BackupSourceRepository sourceRepository;

    @Autowired
    public RestBackupSourcesController(BackupSourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @GetMapping
    // we return an array (and not a List) to make item type accessible to JSON serialization
    public BackupSource[] getAll() {
        List<BackupSource> sources = sourceRepository.findAll();
        return sources.toArray(new BackupSource[sources.size()]);
    }

    @GetMapping("/{id}")
    public BackupSource get(@PathVariable String id) {
        return sourceRepository.findOne(id);
    }

}
