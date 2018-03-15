package org.ogerardin.b2b.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.batch.core.JobParameter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents a set of local files to be backed up
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FilesystemSource extends BackupSource {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** root directory to be backed up */
    private List<Path> paths;

    public FilesystemSource() {
    }

    // results of the latest computation of source file count and total size (for information)
    private long totalBytes = 0;
    private int totalFiles = 0;

    public FilesystemSource(List<Path> paths) {
        this.paths = paths;
    }

    @Override
    public void populateParams(Map<String, JobParameter> params) {
        params.put("source.type", new JobParameter(FilesystemSource.class.getName()));
        try {
            // store param value as JSON array
            params.put("source.roots", new JobParameter(OBJECT_MAPPER.writeValueAsString(paths)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Folders: " + Arrays.toString(getPaths().toArray());
    }

    @Override
    public boolean shouldStartJob() {
        return true;
    }
}

