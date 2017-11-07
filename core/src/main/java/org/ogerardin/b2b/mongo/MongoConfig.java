package org.ogerardin.b2b.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.CustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig
{
    @Bean
    public CustomConversions customConversions()
    {
        return new CustomConversions(Arrays.asList(
                new StringToPathConverter(),
                new PathToStringConverter()
        ));
    }
}