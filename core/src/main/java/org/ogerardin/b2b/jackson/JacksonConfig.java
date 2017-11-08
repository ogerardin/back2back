package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Custom configuration for Jackson (JSON serialization)
 */
@Configuration
public class JacksonConfig {

    /**
     * Provides a {@link com.fasterxml.jackson.databind.Module} that adds custom
     * serializers and deserializers. This bean will be picked up by Spring and used to configure its
     * {@link com.fasterxml.jackson.databind.ObjectMapper}.
     *
     */
    @Bean
    public static Module customSerializationModule() {
        SimpleModule module = new SimpleModule();
        // Jackson handles {@link Path} serialization/deserialization correctly out of the box, but it wraps
        // the path in a URI. Here we just provide a serializer and deserializer that handle the native form
        // of the path.
        module.addSerializer(Path.class, new PathSerializer());
        module.addDeserializer(Path.class, new PathDeserializer());
        return module;
    }

}
