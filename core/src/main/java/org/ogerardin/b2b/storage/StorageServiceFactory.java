package org.ogerardin.b2b.storage;

public interface StorageServiceFactory<S extends StorageService> {

    /**
     * @return a StorageService object associated with the specified id. How the id translates in terms of storage is
     * dependent on the specific StorageService implementation; for example for GridFsStorageService it will be used
     * as the GridFS bucket name.
     */
    S getStorageService(String id);

}
