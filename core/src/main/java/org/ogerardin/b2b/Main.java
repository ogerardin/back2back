package org.ogerardin.b2b;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.batch.jobs.JobStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
public class Main {

    private static final Log logger = LogFactory.getLog(Main.class);
    private static String[] args;
    private static ConfigurableApplicationContext context;

    @Autowired
    private JobStarter jobStarter;

    public Main() {
    }

    public static void main(String[] args) {
        Main.args = args;
        Main.context = SpringApplication.run(Main.class, args);
    }

    public static void restart() {
        // close previous context
        context.close();

        // and build new one using the new mode
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        Main.context = builder.build().run(Main.args);
    }

    @Bean
    CommandLineRunner init() {
        return (args) -> {
            jobStarter.startAllJobs();
        };
    }



}
