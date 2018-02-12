package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Represents a set of local files to be backed up
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FilesystemSource extends BackupSource {

    /** root directory to be backed up */
    private Path path;

    public FilesystemSource() {
    }

    public FilesystemSource(String dir) {
        this(Paths.get(dir));
    }

    public FilesystemSource(Path path) {
        this.path = path;
    }

    public FilesystemSource(File dir) {
        this(dir.toPath());
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("source.type", new JobParameter(FilesystemSource.class.getName()));
        params.put("source.root", new JobParameter(path.toString()));
    }

    @Override
    public String getDescription() {
        return "Local folder " + getPath();
    }
}

