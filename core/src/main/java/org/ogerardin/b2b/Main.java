package org.ogerardin.b2b;

import org.ogerardin.b2b.batch.JobStarter;
import org.ogerardin.b2b.config.ConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
public class Main {

    private static String[] args;
    private static ConfigurableApplicationContext context;

    @Autowired
    B2BProperties properties;

    @Autowired
    private JobStarter jobStarter;

    @Autowired
    private ConfigManager configManager;

    public Main() {
    }

    public static void main(String[] args) {
        Main.args = args;
        Main.context = SpringApplication.run(Main.class, args);
    }

    public static void restart() {
        // close previous context and start a new one
        context.close();
        Main.context = SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return args -> {
            configManager.init();
            if (properties.startJobs) {
                jobStarter.startAllJobs();
            }
        };
    }



}
