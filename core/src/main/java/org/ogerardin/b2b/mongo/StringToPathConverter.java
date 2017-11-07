package org.ogerardin.b2b.mongo;

import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class StringToPathConverter implements Converter<String, Path> {
    @Override
    public Path convert(String source) {
        return Paths.get(source);
    }
}
