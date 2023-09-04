package world.trecord.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "sseTaskExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor messagingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(200);
        return executor;
    }
}
