package org.ogerardin.b2b.storage;

import java.nio.file.Path;
import java.util.List;

@FunctionalInterface
public interface RevisionLister {
    List<RevisionInfo> getAllRevisions(Path path);
}
