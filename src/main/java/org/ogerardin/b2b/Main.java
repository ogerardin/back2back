package org.ogerardin.b2b;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ogerardin.b2b.config.BackupSourceRepository;
import org.ogerardin.b2b.config.BackupTargetRepository;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.worker.BackupWorkerBase;
import org.ogerardin.b2b.worker.BackupWorkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"org.ogerardin.b2b"})
public class Main {

    private static final Log logger = LogFactory.getLog(Main.class);

    private final BackupSourceRepository sourceRepository;
    private final BackupTargetRepository targetRepository;

    private final TaskExecutor taskExecutor;

    private final BackupWorkerFactory backupWorkerFactory;


    @Autowired
    public Main(BackupSourceRepository sourceRepository, BackupTargetRepository targetRepository, TaskExecutor taskExecutor, BackupWorkerFactory backupWorkerFactory) {
        this.sourceRepository = sourceRepository;
        this.targetRepository = targetRepository;
        this.taskExecutor = taskExecutor;
        this.backupWorkerFactory = backupWorkerFactory;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.init();

            startAllWorkers();
        };
    }

    private void startAllWorkers() {
        sourceRepository.findAll().forEach(this::startWorkers);
    }

    private void startWorkers(BackupSource source) {
        targetRepository.findAll().forEach(target -> {
            try {
                startWorker(source, target);
            } catch (B2BException e) {
                logger.error("Failed to start worker for " + source + ", " + target, e);
            }
        });

    }

    private void startWorker(BackupSource source, BackupTarget target) throws B2BException {

        BackupWorkerBase worker = backupWorkerFactory.newWorker(source, target);

        taskExecutor.execute(worker);

    }


}
