package org.ogerardin.b2b.storage;

import java.nio.file.Path;

public interface Storer {
    void store(Path path);
}
