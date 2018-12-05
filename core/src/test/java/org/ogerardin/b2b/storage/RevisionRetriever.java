package org.ogerardin.b2b.storage;

import java.io.IOException;
import java.io.InputStream;

public interface RevisionRetriever {
    InputStream getAsInputStream(String revisionId) throws StorageFileRevisionNotFoundException, IOException, StorageFileNotFoundException;
}
