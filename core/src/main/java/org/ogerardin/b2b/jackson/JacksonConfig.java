package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

/**
 * Custom configuration for Jackson (JSON serialization for REST services)
 */
@Configuration
public class JacksonConfig {

    /**
     * Provides a {@link com.fasterxml.jackson.databind.Module} that adds custom
     * Jackson configuration. This bean will be picked up by Spring and used to configure its
     * {@link com.fasterxml.jackson.databind.ObjectMapper}.
     */
    @Bean
    public static Module customModule() {
        SimpleModule module = new SimpleModule();
        // Jackson handles {@link Path} serialization/deserialization correctly out of the box, but it wraps
        // the path in a URI. Here we just provide a serializer and deserializer that handle the native form
        // of the pathto make the JSON a bit lighter.
        module.addSerializer(Path.class, new PathSerializer());
        module.addDeserializer(Path.class, new PathDeserializer());
        // We need to take some precautions when deserializing StepExecution otherwise we get a StackOverflowError
        // because of circular references. Since we don't have access to the problematic class, we get around this
        // by using a mixin
        module.setMixInAnnotation(StepExecution.class, StepExecutionMixin.class);
        return module;
    }

}
