package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.nio.file.Path;

/**
 * Custom configuration for Jackson (JSON serialization)
 */
//@Configuration
public class JacksonConfig {

    /**
     * Provides a {@link com.fasterxml.jackson.databind.Module} that adds custom
     * serializers and deserializers.
     */
//    @Bean
    public static Module customSerializationModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Path.class, new PathSerializer());
        module.addDeserializer(Path.class, new PathDeserializer());
        return module;
    }

}
