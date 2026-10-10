package com.storix.domain.domains.search.publisher;

import com.storix.domain.domains.search.event.FeedIndexEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedIndexPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishSaved(Long boardId) {
        publish(new FeedIndexEvent(boardId, false));
    }

    public void publishDeleted(Long boardId) {
        publish(new FeedIndexEvent(boardId, true));
    }

    private void publish(FeedIndexEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.warn(">>> [FeedIndex] 이벤트 발행 실패 boardId={}, deleted={}", event.boardId(), event.deleted(), e);
        }
    }
}
