package org.ogerardin.b2b.api;

import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
