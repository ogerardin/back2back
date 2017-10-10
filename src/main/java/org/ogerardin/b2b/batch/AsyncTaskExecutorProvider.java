package org.ogerardin.b2b.batch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Provide an {@link AsyncTaskExecutor} intended for the Spring batch {@link org.springframework.batch.core.launch.JobLauncher}
 */
@Configuration
public class AsyncTaskExecutorProvider {

    @Bean
    public static AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        return executor;
    }
}
