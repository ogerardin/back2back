package org.ogerardin.b2b.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class FileSource extends Source {

    @Id
    String id;

    private List<String> paths;

    public FileSource(String... paths) {
        this.paths = Arrays.stream(paths)
                .collect(Collectors.toList());
    }
    public FileSource(List<String> paths) {
        this.paths = paths;
    }

    public FileSource() {
    }
}

