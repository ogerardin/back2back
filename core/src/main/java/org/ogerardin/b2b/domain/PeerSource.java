package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a remote peer source, i.e. a remote computer backing up to this computer.
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
        return "Peer back2back source from " + getRemoteComputerId();
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        //unused
    }
}
