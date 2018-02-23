package org.ogerardin.b2b.storage;

public interface StorageServiceFactory<S extends StorageService> {

    /**
     * Returns a StorageService object associated with the specified name.
     */
    S getStorageService(String name);

}
