package org.ogerardin.b2b.domain;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.domain.entity.FilesystemSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BackupSetManager {

    @Autowired
    private BackupSetRepository backupSetRepository;

    @Autowired
    private BackupSourceRepository backupSourceRepository;

    @Autowired
    private BackupTargetRepository backupTargetRepository;

    /**
     * Sanity checks
     */
    public void init() {
        // make sure we have a FilesystemSource
        long count = backupSourceRepository.findAll().stream()
                .filter(bs -> bs.getClass() == FilesystemSource.class)
                .count();
        if (count == 0) {
            log.info("Creating initial FilesystemSource");
            backupSourceRepository.insert(new FilesystemSource());
        }

        // reset the stored status of all backup sets
        backupSetRepository.findAll()
                .forEach(s -> {
                    s.resetState();
                    backupSetRepository.save(s);
                });
    }


}
