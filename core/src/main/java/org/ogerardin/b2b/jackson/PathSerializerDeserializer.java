package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * By default Jackson handles {@link Path} serialization/deserialization by wrapping the path in a URI;
 * this pair of serializer/deserializer makes it use the default String representation of the Path.
 */
@JsonComponent
public class PathSerializerDeserializer {

    /**
     * Serializer for a {@link Path} object.
     * Uses the default filesystem representation of the path
     */
    @SuppressWarnings("unused")
    public static class PathSerializer extends JsonSerializer<Path> {
        @Override
        public void serialize(Path value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString());
        }
    }

    /**
     * Deserializer for a {@link Path} object.
     * Assumes the path is formatted according to the local default filesystem.
     */
    @SuppressWarnings("unused")
    public static class PathDeserializer extends JsonDeserializer<Path> {

        @Override
        public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Paths.get(p.getValueAsString());
        }

    }
}
