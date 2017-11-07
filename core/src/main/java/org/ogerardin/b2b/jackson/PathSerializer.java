package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Serializer for a {@link java.nio.file.Path} object.
 * Uses the default String representation of the Path, which is filesystem-dependant.
 */
public class PathSerializer extends JsonSerializer<Path> {

    @Override
    public void serialize(Path value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(value.toString());
    }
}
