package org.ogerardin.b2b.api;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.JobStarter;
import org.ogerardin.b2b.domain.entity.BackupSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.util.CandidateClassScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sources")
@Slf4j
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
        Optional<BackupSource> backupSource = sourceRepository.findById(id);
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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws NotFoundException {
        assertExists(id);
        sourceRepository.deleteById(id);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
    }

    private void assertExists(@PathVariable String id) throws NotFoundException {
        if (!sourceRepository.existsById(id)) {
            throw new NotFoundException(id);
        }
    }

    /**
     * @return an array of sample instances of BackupSources, one for each concrete implementation of {@link BackupSource}
     * This might be used to dynamically construct the UI.
     */
    @GetMapping("/types")
    public BackupSource[] getTypes() {
        Collection<Class<? extends BackupSource>> backupSourceClasses =
                CandidateClassScanner.getAssignableClasses(BackupSource.class, "org.ogerardin.b2b.domain.entity");
        List<BackupSource> backupTargets = new ArrayList<>();
        for (Class<? extends BackupSource> backupSourceClass : backupSourceClasses) {
            BackupSource backupSource = null;
            try {
                backupSource = backupSourceClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Failed to instantiate {}", backupSourceClass);
            }
            backupTargets.add(backupSource);
        }
        return backupTargets.toArray(new BackupSource[0]);
    }


}
