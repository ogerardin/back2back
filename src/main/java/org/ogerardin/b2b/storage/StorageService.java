package org.ogerardin.b2b.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Stream<Path> loadAll();

    InputStream getAsInputStream(String filename) throws FileNotFoundException;

    Resource getAsResource(String filename) throws FileNotFoundException;

    void deleteAll();

}
