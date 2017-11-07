package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Deserializer for a {@link Path} object.
 * Assumes the path is formatted according to the local default filesystem
 */
public class PathDeserializer extends JsonDeserializer<Path> {

    @Override
    public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return Paths.get(p.getValueAsString());
    }
}
