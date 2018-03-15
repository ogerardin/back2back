package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a remote peer source, i.e. a remote computer backing up to this computer.
 * This source is not meant to be user-configurable, but is created automatically the first time an new peer
 * saves to this computer.
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
        return "Peer instance with ID " + getRemoteComputerId();
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("source.type", new JobParameter(PeerSource.class.getName()));
    }

    @Override
    public boolean shouldStartJob() {
        // a Peer source is passive (it waits for remote requests) hence we mustn't start a local job for it.
        return false;
    }
}
