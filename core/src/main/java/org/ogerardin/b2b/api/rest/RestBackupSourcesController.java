package org.ogerardin.b2b.api.rest;

import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
        return sources.toArray(new BackupSource[0]);
    }

    @GetMapping("/{id}")
    public BackupSource get(@PathVariable String id) {
        return sourceRepository.findOne(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String create(@RequestBody BackupSource source) {
        BackupSource savedSource = sourceRepository.insert(source);
        //TODO: adding a source should trigger an update of current jobs
        return savedSource.getId();
    }

/*
    @PostMapping("/{id}/path")
    public void setPath(@PathVariable String id, String path) {
        BackupSource source = sourceRepository.findOne(id);
        if (source == null) {
            throw new RuntimeException("Failed to find id " + id);
        }
        if (! (source instanceof FilesystemSource)) {
            throw new RuntimeException("Source is not a " + FilesystemSource.class.getSimpleName() + ", id " + id);
        }
        FilesystemSource filesystemSource = (FilesystemSource) source;
        filesystemSource.setPath(Paths.get(path));
        sourceRepository.save(filesystemSource);
    }
*/

    @DeleteMapping ("/{id}")
    public void delete(@PathVariable String id) {
         sourceRepository.delete(id);
    }

}
