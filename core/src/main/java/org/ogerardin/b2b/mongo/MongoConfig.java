package org.ogerardin.b2b.mongo;

import org.ogerardin.b2b.mongo.cascade.CascadeSaveMongoEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.Arrays;

/**
 * Custom configuration for Spring Data MongoDB
 */
@Configuration
public class MongoConfig
{
    /**
     * Prodives a set of custom conversions that are used for MongoDB serialization/deserialization.
     */
    @Bean
    public CustomConversions customConversions()
    {
        return new CustomConversions(Arrays.asList(
                new StringToPathConverter(),
                new PathToStringConverter()
        ));
    }

    @Bean
    public CascadeSaveMongoEventListener userCascadingMongoEventListener() {
        return new CascadeSaveMongoEventListener();
    }
}