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
        executor.setTaskDecorator(mdcTaskDecorator());

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
        executor.setTaskDecorator(mdcTaskDecorator());

        // 버리면 토픽룸 최신 메시지가 안 바뀌고 푸시 이벤트도 안 나간다
        executor.setRejectedExecutionHandler((r, exec) -> {
            recordRejected("chatAsync");
            log.warn(">>> [Chat] queue overflow - running on caller thread (pool={}, queue={}/{})",
                    exec.getPoolSize(), exec.getQueue().size(), exec.getQueue().remainingCapacity());
            if (!exec.isShutdown()) {
                r.run();
            }
        });
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
        executor.setTaskDecorator(mdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean(name = "s3CleanupExecutor")
    public Executor s3CleanupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("S3Cleanup-");
        executor.setTaskDecorator(mdcTaskDecorator());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.setRejectedExecutionHandler((r, exec) -> {
            log.error(">>> [S3Cleanup] queue overflow — cleanup task dropped, 고아 오브젝트 발생 가능 (pool={}, queue={}/{})",
                    exec.getPoolSize(), exec.getQueue().size(), exec.getQueue().remainingCapacity());
        });
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

    @Bean(name = "topicRoomPushExecutor")
    public Executor topicRoomPushExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("TopicRoomPush-");
        executor.setTaskDecorator(mdcTaskDecorator()); // MDC 전파

        // 버려도 앵커가 안 옮겨져 sweep 이 줍긴 하지만 그만큼 발송이 밀린다
        executor.setRejectedExecutionHandler((r, exec) -> {
            recordRejected("topicRoomPush");
            log.warn(">>> [TopicRoomPush] queue overflow - running on caller thread (pool={}, queue={}/{})",
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
