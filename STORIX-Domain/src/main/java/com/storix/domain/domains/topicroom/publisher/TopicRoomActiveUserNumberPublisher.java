package com.storix.domain.domains.topicroom.publisher;

import com.storix.domain.domains.topicroom.event.TopicRoomActiveUserNumberChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomActiveUserNumberPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(Long topicRoomId, Integer activeUserNumber) {
        try {
            eventPublisher.publishEvent(TopicRoomActiveUserNumberChangedEvent.of(topicRoomId, activeUserNumber));
        } catch (Exception e) {
            log.warn(">>> [TopicRoomActiveUserNumber] publish failed topicRoomId={}, activeUserNumber={}, cause={}",
                    topicRoomId, activeUserNumber, e.getMessage());
        }
    }
}
