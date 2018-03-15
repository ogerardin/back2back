package org.ogerardin.b2b.api;

import lombok.Builder;
import lombok.Data;
import org.ogerardin.b2b.domain.FilesystemSource;
import org.ogerardin.b2b.domain.PeerSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final BackupSetRepository backupSetRepository;

    @Autowired
    public DashboardController(BackupSetRepository backupSetRepository) {
        this.backupSetRepository = backupSetRepository;
    }

    @Data
    @Builder
    static class Progress {
        String name;
        @Builder.Default
        int percent = 0;
        @Builder.Default
        String message ="";
    }

    @GetMapping("/destinations")
    public Progress[] destinations() {
        return backupSetRepository.findAll().stream()
                .filter(bs -> bs.getBackupSource() instanceof FilesystemSource)
                .map(bs -> Progress.builder()
                        .name("Backup up to: " + bs.getBackupTarget().getDescription())
                        .percent(100)
                        .message(bs.getStatus())
                        .build()
                ).toArray(Progress[]::new);
    }

    @GetMapping("/incoming")
    public Progress[] incoming() {
        return backupSetRepository.findAll().stream()
                .filter(bs -> bs.getBackupSource() instanceof PeerSource)
                .map(bs -> Progress.builder()
                        .name("Backup from: " + bs.getBackupSource().getDescription())
                        .percent(100)
                        .message(bs.getStatus())
                        .build()
                ).toArray(Progress[]::new);
    }

}
