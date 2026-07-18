package com.storix;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.storix.infrastructure.config.SecurityConfig;
import com.storix.infrastructure.config.WebSocketConfig;
import com.storix.infrastructure.external.chat.RedisSubscriber;
import com.storix.infrastructure.external.chat.StompHandler;
import com.storix.infrastructure.external.topicroom.TopicRoomActiveUserNumberRedisSubscriber;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@ComponentScan(
        basePackages = "com.storix",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        SecurityConfig.class,
                        WebSocketConfig.class,
                        StompHandler.class,
                        RedisSubscriber.class,
                        TopicRoomActiveUserNumberRedisSubscriber.class
                }
        )
)
public class StorixBatchApplication {

    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(StorixBatchApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
