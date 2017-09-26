package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilesystemSource extends BackupSource {

    private List<String> paths;

    public FilesystemSource(String... paths) {
        this.paths = Arrays.stream(paths)
                .collect(Collectors.toList());
    }
    public FilesystemSource(List<String> paths) {
        this.paths = paths;
    }

    public FilesystemSource() {
    }

    public FilesystemSource(File dir) throws IOException {
        this(dir.getCanonicalPath());
    }
}

