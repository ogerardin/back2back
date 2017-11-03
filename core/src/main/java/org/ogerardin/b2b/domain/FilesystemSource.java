package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Represents a set of local files to be backed up
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FilesystemSource extends BackupSource {

    /** root directory to be backed up */
    // FIXME should be a java.nio.file.Path but causes serialization failure
    private String path;

    public FilesystemSource() {
    }

    public FilesystemSource(String path) {
        this.path = path;
    }

    public FilesystemSource(File dir) throws IOException {
        this(dir.getCanonicalPath());
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("source.type", new JobParameter(FilesystemSource.class.getName()));
        params.put("source.root", new JobParameter(path));
    }
}

