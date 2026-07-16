package com.storix.infrastructure.config;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final String METRIC_REJECTED = "notification.executor.rejected";
    private static final String TAG_EXECUTOR = "executor";

    private final MeterRegistry meterRegistry;

    public AsyncConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // 큐 포화로 태스크가 거절될 때 executor별 카운트
    private void recordRejected(String executor) {
        meterRegistry.counter(METRIC_REJECTED, TAG_EXECUTOR, executor).increment();
    }

    // 제출 스레드의 MDC(상관키 등)를 워커 스레드로 복사 - @Async 경계에서 로그 상관키 유지
    private static TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> submitted = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                if (submitted != null) {
                    MDC.setContextMap(submitted);
                } else {
                    MDC.clear();
                }
                try {
                    runnable.run();
                } finally {
                    if (previous != null) {
                        MDC.setContextMap(previous);
                    } else {
                        MDC.clear();
                    }
                }
            };
        };
    }

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

    @Bean(name = "notificationConsumerExecutor")
    public Executor notificationConsumerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("NotificationConsumer-");
        executor.setTaskDecorator(mdcTaskDecorator()); // MDC 전파
        
        executor.setRejectedExecutionHandler((r, exec) -> {
            recordRejected("notificationConsumer");
            log.warn(">>> [Notification] queue overflow - running on caller thread (pool={}, queue={}/{})",
                    exec.getPoolSize(), exec.getQueue().size(), exec.getQueue().remainingCapacity());
            if (!exec.isShutdown()) {
                r.run();
            }
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "adminNotificationProducerExecutor")
    public Executor adminNotificationProducerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("AdminNotificationProducer-");
        executor.setTaskDecorator(mdcTaskDecorator()); // MDC 전파

        executor.setRejectedExecutionHandler((r, exec) -> {
            recordRejected("adminNotificationProducer");
            log.warn(">>> [AdminNotification] broadcast queue overflow - running on caller thread (pool={}, queue={}/{})",
                    exec.getPoolSize(), exec.getQueue().size(), exec.getQueue().remainingCapacity());
            if (!exec.isShutdown()) {
                r.run();
            }
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "adminNotificationConsumerExecutor")
    public Executor adminNotificationConsumerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AdminNotificationConsumer-");
        executor.setTaskDecorator(mdcTaskDecorator()); // MDC 전파

        executor.setRejectedExecutionHandler((r, exec) -> {
            recordRejected("adminNotificationConsumer");
            log.warn(">>> [AdminNotification] queue overflow - running on caller thread (pool={}, queue={}/{})",
                    exec.getPoolSize(), exec.getQueue().size(), exec.getQueue().remainingCapacity());
            if (!exec.isShutdown()) {
                r.run();
            }
        });
        executor.initialize();
        return executor;
    }
}
