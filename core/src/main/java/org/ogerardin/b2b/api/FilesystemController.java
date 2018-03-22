package org.ogerardin.b2b.api;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This controller is meant to interact with the local filesystem, to allow browsing files from a web UI.
 */
@RestController
@RequestMapping("/api/filesystem")
public class FilesystemController {

    @Data
    public static class FSItem {
        String name;
        Path path;
        boolean directory;

        FSItem() {
        }

        public FSItem(Path path, boolean directory) {
            this(path.getFileName().toString(), path, directory);
        }

        public FSItem(String name, Path path, boolean directory) {
            this.name = name;
            this.path = path;
            this.directory = directory;
        }
    }

    @GetMapping()
    FSItem[] list(@RequestParam(required = false) String dir,
                  @RequestParam(required = false) boolean dirOnly) throws IOException {
        if (dir == null) {
            dir = System.getProperty("user.home");
        }
        Path dirPath = Paths.get(dir).toAbsolutePath();

        List<FSItem> fsItems = Files.list(dirPath)
                .filter(Files::isReadable)
                .map(p -> new FSItem(p, Files.isDirectory(p)))
                .filter(fsi -> !dirOnly || fsi.isDirectory())
                .collect(Collectors.toList());

        // if the specified directory is not a root, add parent directory as first item
        if (dirPath.getParent() != null) {
            fsItems.add(0, new FSItem("⬑", dirPath.getParent(), true));
        }
        return fsItems.toArray(new FSItem[0]);
    }

    /**
     * Return the set of filesystem roots. On Windows this will be the drives C:\, D:\, ...
     */
    @GetMapping("/roots")
    FSItem[] roots() {
        List<FSItem> fsItems = new ArrayList<>();
        for (Path path : FileSystems.getDefault().getRootDirectories()) {
            if (Files.isReadable(path)) {
                fsItems.add(new FSItem(path.toString(), path, true));
            }
        }
        return fsItems.toArray(new FSItem[0]);
    }
}
