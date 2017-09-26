package org.ogerardin.b2b;

import org.ogerardin.b2b.config.BackupSourceRepository;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.gridfs.GridFsStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
@EnableConfigurationProperties({GridFsStorageProperties.class})
public class Main {


    @Autowired
    BackupSourceRepository sourceRepository;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init(@Qualifier("gridFsStorageProvider") StorageService storageService) {
        return (args) -> {
            storageService.init();
        };
    }



}
