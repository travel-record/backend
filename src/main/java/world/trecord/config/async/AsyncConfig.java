package world.trecord.config.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();

        log.info("Initializing async executor");
        log.info("Available processors count: {}", processors);

        executor.setCorePoolSize(processors);
        log.info("Setting core pool size: {}", processors);

        executor.setMaxPoolSize(processors * 2);
        log.info("Setting max pool size: {}", processors * 2);

        executor.setQueueCapacity(50);
        log.info("Setting queue capacity: 50");

        executor.setKeepAliveSeconds(60);
        log.info("Setting keep alive seconds: 60");

        executor.setThreadNamePrefix("AsyncExecutor-");
        log.info("Setting thread name prefix: AsyncExecutor-");

        executor.initialize();
        log.info("Async executor initialized");

        return executor;
    }
}
