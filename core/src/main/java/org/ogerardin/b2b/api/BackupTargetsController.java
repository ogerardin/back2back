package org.ogerardin.b2b.api;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.JobStarter;
import org.ogerardin.b2b.domain.entity.BackupTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.ogerardin.b2b.util.CandidateClassScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/targets")
@Slf4j
public class BackupTargetsController {

    private final BackupTargetRepository targetRepository;

    private final JobStarter jobStarter;

    @Autowired
    public BackupTargetsController(BackupTargetRepository targetRepository, JobStarter jobStarter) {
        this.targetRepository = targetRepository;
        this.jobStarter = jobStarter;
    }

    @GetMapping
    // we return an array (and not a List) to make item type accessible to JSON serialization
    public BackupTarget[] getAll() {
        List<BackupTarget> targets = targetRepository.findAll();
        return targets.toArray(new BackupTarget[0]);
    }

    @GetMapping("/{id}")
    public BackupTarget get(@PathVariable String id) throws NotFoundException {
        Optional<BackupTarget> backupTarget = targetRepository.findById(id);
        return backupTarget.orElseThrow(() -> new NotFoundException(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BackupTarget create(@RequestBody BackupTarget target) {
        BackupTarget savedTarget = targetRepository.save(target);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
        return savedTarget;
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BackupTarget update(@PathVariable String id,
                               @RequestBody BackupTarget target) throws NotFoundException {
        assertExists(id);
        target.setId(id);
        BackupTarget savedTarget = targetRepository.save(target);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
        return savedTarget;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws NotFoundException {
        assertExists(id);
        targetRepository.deleteById(id);
        //TODO: use change streams? (requires Spring data MongoDB 2.1) https://docs.spring.io/spring-data/mongodb/docs/2.1.0.M3/reference/html/#change-streams
        jobStarter.syncJobs();
    }

    private void assertExists(@PathVariable String id) throws NotFoundException {
        if (!targetRepository.existsById(id)) {
            throw new NotFoundException(id);
        }
    }

    /**
     * @return an array of sample instances of BackupTargets, one for each concrete implementation of {@link BackupTarget}
     * This might be used to dynamically construct the UI.
     */
    @GetMapping("/types")
    public BackupTarget[] getTypes() {
        Collection<Class<? extends BackupTarget>> backuptargetClasses =
                CandidateClassScanner.getAssignableClasses(BackupTarget.class, "org.ogerardin.b2b.domain.entity");
        List<BackupTarget> backupTargets = new ArrayList<>();
        for (Class<? extends BackupTarget> backuptargetClass : backuptargetClasses) {
            BackupTarget newInstance = null;
            try {
                newInstance = backuptargetClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Failed to instantiate {}", backuptargetClass);
            }
            backupTargets.add(newInstance);
        }
        return backupTargets.toArray(new BackupTarget[0]);
    }

}
