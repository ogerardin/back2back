package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.util.Map;

/**
 * Represents a local filesystem backup destination
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FilesystemTarget extends BackupTarget {

    public FilesystemTarget() {
    }

    /** destination folder */
    // FIXME should be a java.nio.file.Path but causes serialization failure
    String path;

    public FilesystemTarget(String path) {
        this.path = path;
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("target.type", new JobParameter(FilesystemTarget.class.getName()));
        params.put("target.path", new JobParameter(path));
    }
}
