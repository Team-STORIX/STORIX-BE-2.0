package com.storix.domain.domains.notification.publisher;

import com.storix.domain.domains.notification.event.AdminNotificationChunkEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishChunk(AdminNotificationChunkEvent event) {
        eventPublisher.publishEvent(event);
    }
}
