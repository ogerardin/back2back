package org.ogerardin.b2b.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

@Data
public class BackupSet {

    @Id
    private String id;

    private UUID computerId;

    private Instant lastBackupCompleteTime;
    private Instant currentBackupStartTime;
}
