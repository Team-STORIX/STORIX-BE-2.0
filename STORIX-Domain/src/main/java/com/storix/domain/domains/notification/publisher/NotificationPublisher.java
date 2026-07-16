package com.storix.domain.domains.notification.publisher;

import com.storix.domain.domains.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 도메인 서비스(피드/리뷰/댓글)에서 알림 이벤트 발행
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final ApplicationEventPublisher eventPublisher;

    // 알림 이벤트 발행
    public void publish(NotificationEvent event) {
        if (event == null || event.recipientUserId() == null) {
            log.warn(">>> [Notification] invalid event dropped: {}", event);
            return;
        }
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.warn(">>> [Notification] publish failed recipient={}, type={}, cause={}",
                    event.recipientUserId(), event.notificationType(), e.getMessage());
        }
    }

    // 본인 글에 본인이 좋아요/댓글을 달 경우 알림 발행 X (actorUserId == recipientUserId)
    public void publishUnlessSelf(Long actorUserId, NotificationEvent event) {
        if (actorUserId != null && event != null
                && actorUserId.equals(event.recipientUserId())) {
            return;
        }
        publish(event);
    }
}
