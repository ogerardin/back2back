package org.ogerardin.b2b.web;

import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Allows the use of an {@link InputStream} as a {@link org.springframework.core.io.Resource} for a file part in a
 * multipart message, as an alternative to {@link org.springframework.core.io.PathResource}.
 */
public class InputStreamFileResource extends InputStreamResource {

    private final String filename;

    public InputStreamFileResource(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }
    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
        return -1; // we do not want to generally read the whole stream into memory ...
    }
}