package org.ogerardin.b2b.storage;

import java.io.InputStream;
import java.nio.file.Path;

public interface Retriever {
    InputStream getAsInputStream(Path path);
}
