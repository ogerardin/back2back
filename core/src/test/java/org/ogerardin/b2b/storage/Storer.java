package org.ogerardin.b2b.storage;

import java.nio.file.Path;

@FunctionalInterface
public interface Storer {
    String store(Path path);
}
