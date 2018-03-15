package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.util.Map;

/**
 * Represents the local (internal) backup destination.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LocalTarget extends BackupTarget {

    public LocalTarget() {
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("target.type", new JobParameter(LocalTarget.class.getName()));
    }

    @Override
    public String getDescription() {
        return "Internal storage";
    }
}
