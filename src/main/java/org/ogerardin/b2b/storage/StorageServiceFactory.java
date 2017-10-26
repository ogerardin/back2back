package org.ogerardin.b2b.storage;

public interface StorageServiceFactory {

    StorageService getStorageService(String name);

}
