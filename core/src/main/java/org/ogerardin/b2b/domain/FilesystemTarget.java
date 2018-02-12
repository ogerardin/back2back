package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents a local filesystem backup destination
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FilesystemTarget extends BackupTarget {

    /** destination folder */
    Path path;

    public FilesystemTarget() {
    }

    public FilesystemTarget(Path path) {
        this.path = path;
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("target.type", new JobParameter(FilesystemTarget.class.getName()));
        params.put("target.path", new JobParameter(path.toString()));
    }

    @Override
    public String getDescription() {
        return "Local folder " + getPath();
    }
}
