package org.ogerardin.b2b.api;

import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/targets")
public class BackupTargetsController {

    private final BackupTargetRepository targetRepository;

    @Autowired
    public BackupTargetsController(BackupTargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    @GetMapping
    // we return an array (and not a List) to make item type accessible to JSON serialization
    public BackupTarget[] getAll() {
        List<BackupTarget> targets = targetRepository.findAll();
        return targets.toArray(new BackupTarget[0]);
    }

    @GetMapping("/{id}")
    public BackupTarget get(@PathVariable String id) {
        return targetRepository.findOne(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BackupTarget create(@RequestBody BackupTarget target) {
        BackupTarget savedTarget = targetRepository.save(target);
        //TODO: adding a target should trigger an update of current jobs
        return savedTarget;
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BackupTarget update(@PathVariable String id,
                               @RequestBody BackupTarget target) throws NotFoundException {
        assertExists(id);
        target.setId(id);
        BackupTarget savedTarget = targetRepository.save(target);
        //TODO: updating a target should trigger an update of current jobs
        return savedTarget;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) throws NotFoundException {
        assertExists(id);
        targetRepository.delete(id);
    }

    private void assertExists(@PathVariable String id) throws NotFoundException {
        if (!targetRepository.exists(id)) {
            throw new NotFoundException(id);
        }
    }
}
