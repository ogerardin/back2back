package org.ogerardin.b2b.storage;

import java.io.InputStream;
import java.nio.file.Path;

@FunctionalInterface
public interface PathRetriever {
    InputStream getAsInputStream(Path path);
}
