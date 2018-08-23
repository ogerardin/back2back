package org.ogerardin.b2b;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.batch.JobStarter;
import org.ogerardin.b2b.config.ConfigManager;
import org.ogerardin.b2b.domain.BackupSetManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableScheduling
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

    @Autowired
    private BackupSetManager backupSetManager;

    public Main() {
    }

    public static void main(String[] args) {
        Main.args = args;
        Main.context = SpringApplication.run(Main.class, args);
    }

    public static void restart() {
        // close previous context and start a new one
        //FIXME this only works once! NPE if called a second time...
        Main.context.close();
        Main.context = SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init() {
        return args -> {
            // load initial config
            configManager.init();

            // reset the state of all backupsets
            backupSetManager.init();

            // start backup jobs
//            jobStarter.syncJobs();
        };
    }

    @Scheduled(fixedRateString = "${org.ogerardin.b2b.job-sync-rate}")
    public void scheduledSync() {
        jobStarter.syncJobs();
    }




}
