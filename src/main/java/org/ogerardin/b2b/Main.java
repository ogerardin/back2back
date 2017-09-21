package org.ogerardin.b2b;

import com.mongodb.MongoClient;
import org.ogerardin.b2b.domain.FileSource;
import org.ogerardin.b2b.repo.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import storage.StorageProperties;
import storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {"storage", "org.ogerardin.b2b"})
@EnableConfigurationProperties(StorageProperties.class)
public class Main {



    @Autowired
    SourceRepository sourceRepository;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.init();
//            storageService.deleteAll();

            sourceRepository.deleteAll();
            sourceRepository.save(new FileSource("/tmp"));
            sourceRepository.findAll().forEach(System.out::println);
        };
    }



}
