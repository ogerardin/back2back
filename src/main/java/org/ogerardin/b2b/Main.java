package org.ogerardin.b2b;

import org.ogerardin.b2b.domain.FileSource;
import org.ogerardin.b2b.repo.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.ogerardin.b2b.storage.filesystem.StorageProperties;
import org.ogerardin.b2b.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
@EnableConfigurationProperties(StorageProperties.class)
public class Main {


    @Autowired
    SourceRepository sourceRepository;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init(@Qualifier("fileSystemStorageService") StorageService storageService) {
        return (args) -> {
            storageService.init();
//            storageService.deleteAll();

            sourceRepository.deleteAll();
            sourceRepository.save(new FileSource("/tmp"));
            sourceRepository.findAll().forEach(System.out::println);
        };
    }



}
