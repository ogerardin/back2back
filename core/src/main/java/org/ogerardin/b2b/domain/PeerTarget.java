package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.util.Map;

/**
 * Represents a remote peer backup destination
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PeerTarget extends BackupTarget {

    String hostname;
    long port;

    public PeerTarget() {
    }

    public PeerTarget(String hostname, long port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("target.type", new JobParameter(PeerTarget.class.getName()));
        params.put("target.hostname", new JobParameter(hostname));
        params.put("target.port", new JobParameter(port));
    }

    @Override
    public String getDescription() {
        return "Peer instance at " + getHostname() + ":" + getPort();
    }
}
