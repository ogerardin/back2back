package org.ogerardin.b2b.files;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * Recursively walks a filesystem directory and collects the {@link Path} of all files.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RecursivePathCollector extends SimpleFileVisitor<Path> {
    private static final Log logger = LogFactory.getLog(RecursivePathCollector.class);

    private final Set<Path> paths = new HashSet<>();

    private final Path rootDir;

    public RecursivePathCollector(Path rootDir) {
        this.rootDir = rootDir;
    }

    public void walkTree() throws IOException {
        logger.info("Collecting all files under " + rootDir);
        Files.walkFileTree(rootDir,this);
        logger.info("Found " + paths.size() + " files");

    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        paths.add(path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        logger.warn("Failed to visit file: " + path + " (" + e + ")");
        return FileVisitResult.CONTINUE;
    }
}
