package org.ogerardin.b2b.api;

import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This controller is meant to interact with the local filesystem, to allow browsing
 * files from a remote UI.
 */
@RestController
@RequestMapping("/api/filesystem")
public class RestFilesystemController {

    @Data
    public static class FSItem {
        Path path;
        boolean directory;

        FSItem() {}

        FSItem(Path path, boolean directory) {
            this.path = path;
            this.directory = directory;
        }
    }

    @GetMapping()
    FSItem[] list(@RequestParam(value = "dir", required = false) String dir) throws IOException {
        if (dir == null) {
            dir = System.getProperty("user.home");
        }
        Path dirPath = Paths.get(dir);

        return Files.list(dirPath)
                .map(dirPath::relativize)
                .map(p -> new FSItem(p, Files.isDirectory(p)))
                .toArray(FSItem[]::new);
    }

}
