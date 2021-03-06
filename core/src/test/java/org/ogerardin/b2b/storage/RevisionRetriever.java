package org.ogerardin.b2b.storage;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface RevisionRetriever {
    InputStream getAsInputStream(String revisionId) throws RevisionNotFoundException, IOException, FileNotFoundException;
}
