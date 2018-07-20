package org.ogerardin.b2b.api;

import org.ogerardin.b2b.batch.JobStarter;
import org.ogerardin.b2b.domain.entity.BackupSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sources")
public class BackupSourcesController {

    private final BackupSourceRepository sourceRepository;

    private final JobStarter jobStarter;

    @Autowired
    public BackupSourcesController(BackupSourceRepository sourceRepository, JobStarter jobStarter) {
        this.sourceRepository = sourceRepository;
        this.jobStarter = jobStarter;
    }

    @GetMapping
    // we return an array (and not a List) to make item type accessible to JSON serialization
    public BackupSource[] getAll() {
        List<BackupSource> sources = sourceRepository.findAll();
        return sources.toArray(new BackupSource[0]);
    }

    @GetMapping("/{id}")
    public BackupSource get(@PathVariable String id) throws NotFoundException {
        Optional<BackupSource> backupSource = Optional.ofNullable(sourceRepository.findOne(id));
        return backupSource.orElseThrow(() -> new NotFoundException(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BackupSource create(@RequestBody BackupSource source) {
        BackupSource savedSource = sourceRepository.save(source);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
        return savedSource;
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BackupSource update(@PathVariable String id,
                               @RequestBody BackupSource source) throws NotFoundException {
        assertExists(id);
        source.setId(id);
        BackupSource savedSource = sourceRepository.save(source);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
        return savedSource;
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws NotFoundException {
        assertExists(id);
        sourceRepository.delete(id);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
    }

    private void assertExists(@PathVariable String id) throws NotFoundException {
        if (!sourceRepository.exists(id)) {
            throw new NotFoundException(id);
        }
    }

}
