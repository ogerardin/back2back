package org.ogerardin.b2b.storage;

public interface StorageServiceFactory<S extends StorageService> {

    S getStorageService(String name);

}
