package org.ogerardin.b2b.storage;

import java.nio.file.Path;
import java.util.List;

public interface Lister {
    List<Path> getAllFiles();
}
