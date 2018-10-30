package org.ogerardin.b2b.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom configuration for Jackson (JSON serialization for REST services)
 */
@Configuration
public class JacksonConfig {

    /**
     * Provides a {@link Module} that adds custom Jackson configuration.
     * This bean will be picked up by Spring and used to configure its {@link ObjectMapper}.
     */
    @Bean
    public static Module customModule() {
        SimpleModule module = new SimpleModule();
        // We need to take some precautions when deserializing StepExecution otherwise we get a StackOverflowError
        // because of circular references. Since we don't have access to the problematic class, we get around this
        // by using a mixin
        module.setMixInAnnotation(StepExecution.class, StepExecutionMixin.class);

        return module;
    }

    /**
     * A Jackson mixin class to enable serialization of {@link StepExecution} without
     * triggering a {@link StackOverflowError}
     */
    abstract static class StepExecutionMixin  {
        // ignore jobExecution to avoid circular reference
        @JsonIgnore
        JobExecution jobExecution;
    }
}
