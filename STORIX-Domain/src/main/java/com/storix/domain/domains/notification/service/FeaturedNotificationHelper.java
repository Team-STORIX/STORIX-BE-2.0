package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.FeaturedNotificationDedupAdaptor;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeaturedNotificationHelper {

    private final FeaturedNotificationDedupAdaptor dedupAdaptor;
    private final NotificationPublisher notificationPublisher;

    // 오늘 처음이면 표시하고 true
    public boolean markTodayFeedIfFirst(Long feedId) {
        return dedupAdaptor.markFeedIfFirstToday(feedId);
    }

    public boolean markHotTopicRoomIfFirst(Long roomId) {
        return dedupAdaptor.markTopicRoomIfFirstToday(roomId);
    }

    // 알림 리스너가 커밋 뒤에 돌아서 트랜잭션이 있어야 발행된다
    @Transactional
    public void notifyTodayFeed(Long feedId, Long authorUserId) {
        notificationPublisher.publish(NotificationEvent.todayFeed(authorUserId, feedId));
    }

    @Transactional
    public void notifyHotTopicRoom(Long roomId, String roomName, List<Long> memberIds) {
        for (Long memberId : memberIds) {
            notificationPublisher.publish(NotificationEvent.hotTopicRoom(memberId, roomId, roomName));
        }
    }
}
