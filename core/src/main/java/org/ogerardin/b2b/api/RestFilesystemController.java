package org.ogerardin.b2b.api;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/filesystem")
public class RestFilesystemController {

    @GetMapping()
    Path[] list(@RequestParam(value = "dir", required = false) String dir) throws IOException {
        if (dir == null) {
            dir = System.getProperty("user.home");
        }
        Path dirPath = Paths.get(dir);
        return Files.list(dirPath).toArray(Path[]::new);
    }

}
