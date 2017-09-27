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

    private String path;

    public FilesystemSource() {
    }

    public FilesystemSource(String path) {
        this.path = path;
    }

    public FilesystemSource(File dir) throws IOException {
        this(dir.getCanonicalPath());
    }
}

