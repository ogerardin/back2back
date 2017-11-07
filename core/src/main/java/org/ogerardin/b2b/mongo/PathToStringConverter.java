package org.ogerardin.b2b.mongo;

import org.springframework.core.convert.converter.Converter;

import java.nio.file.Path;

/**
 * Custom converter from {@link Path} to String.
 * Uses the default String representation of the Path, which is filesystem-dependant.
 */
public class PathToStringConverter implements Converter<Path, String> {
    @Override
    public String convert(Path source) {
        return source.toString();
    }
}
