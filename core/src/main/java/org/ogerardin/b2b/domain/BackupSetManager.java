package org.ogerardin.b2b.domain;

import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackupSetManager {

    @Autowired
    private BackupSetRepository backupSetRepository;

    public void resetAll() {
        backupSetRepository.findAll().stream()
                .forEach(s -> {
                    s.resetState();
                    backupSetRepository.save(s);
                });
    }

}
