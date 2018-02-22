package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a remote peer backup destination
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PeerSource extends BackupSource {

    private UUID remoteComputerId;

    public PeerSource() {
    }

    public PeerSource(UUID remoteComputerId) {
        this.remoteComputerId = remoteComputerId;
    }

    @Override
    public String getDescription() {
        return "Peer back2back instance ID " + getRemoteComputerId();
    }
}
