package org.ogerardin.b2b.api.rest;

import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * This controller is meant to interact with the local filesystem, to allow browsing files from a web UI.
 */
@RestController
@RequestMapping("/api/filesystem")
public class RestFilesystemController {

    @Data
    public static class FSItem {
        String name;
        Path path;
        boolean directory;

        FSItem() {
        }

        public FSItem(Path path, boolean directory) {
            this.name = path.getFileName().toString();
            this.path = path;
            this.directory = directory;
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
        Path dirPath = Paths.get(dir);

        Stream<FSItem> itemStream = Files.list(dirPath)
                .filter(Files::isReadable)
                .map(p -> new FSItem(p, Files.isDirectory(p)))
                .filter(fsi -> !dirOnly || fsi.isDirectory());

        // if the specified directory is not a root, add parent directory as first item
        if (dirPath.getParent() != null) {
            itemStream = Stream.concat(
                    Stream.of(new FSItem("⬑", dirPath.getParent(), true)),
                    itemStream
            );
        }

        return itemStream.toArray(FSItem[]::new);

    }

}
