package com.storix.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "logThreadPool")
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);

        executor.setThreadNamePrefix("Log-Thread-");

        executor.initialize();
        return executor;
    }

    @Bean(name = "chatAsyncExecutor")
    public Executor chatAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ChatAsync-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "slackTaskExecutor")
    public Executor slackTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("SlackAsync-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("NotiAsync-");

        // TODO: 재시도 로직 + traceId 로깅 필요
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn(">>> [Notification] queue overflow — running on caller thread (pool={}, queue={}/{})",
                    exec.getPoolSize(), exec.getQueue().size(), exec.getQueue().remainingCapacity());
            if (!exec.isShutdown()) {
                r.run();
            }
        });
        executor.initialize();
        return executor;
    }
}
