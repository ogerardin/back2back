package org.ogerardin.b2b.mongo;

import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;

public class PathToStringConverter implements Converter<Path, String> {
    @Override
    public String convert(Path source) {
        return source.toString();
    }
}
