package org.ogerardin.b2b.repo;

import org.junit.runner.RunWith;
import org.ogerardin.b2b.domain.FileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class MongoTest implements CommandLineRunner {

    @Autowired
    SourceRepository repo;

    public static void main(String[] args) {
        new SpringApplicationBuilder(MongoTest.class)
                .web(false)
                .run(args);
    }


    @Override
    public void run(String... args) throws Exception {

        repo.deleteAll();

        repo.save(new FileSource("/tmp","/Users/Olivier/Documents"));

        repo.findAll().forEach(System.out::println);


    }
}
