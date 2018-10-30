package org.ogerardin.b2b.mongo;

import org.ogerardin.b2b.mongo.cascade.CascadeSaveMongoEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    public MongoCustomConversions customConversions()
    {
        //noinspection Convert2Lambda,Anonymous2MethodRef
        return new MongoCustomConversions(Arrays.asList(
                new Converter<String, Path>() {
                    @Override
                    public Path convert(String source) {
                        return Paths.get(source);
                    }
                },
                new Converter<Path, String>() {
                    @Override
                    public String convert(Path path) {
                        return path.toString();
                    }
                }
        ));
    }

    @Bean
    public CascadeSaveMongoEventListener userCascadingMongoEventListener() {
        return new CascadeSaveMongoEventListener();
    }
}