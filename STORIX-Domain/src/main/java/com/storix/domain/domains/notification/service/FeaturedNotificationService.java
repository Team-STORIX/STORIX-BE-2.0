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
public class FeaturedNotificationService {

    private final FeaturedNotificationDedupAdaptor dedupAdaptor;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public void notifyTodayFeedIfFirst(Long feedId, Long authorUserId) {
        if (!dedupAdaptor.markFeedIfFirstToday(feedId)) {
            return;
        }
        notificationPublisher.publish(NotificationEvent.todayFeed(authorUserId, feedId));
    }

    @Transactional
    public void notifyHotTopicRoomIfFirst(Long roomId, String roomName, List<Long> memberIds) {
        if (!dedupAdaptor.markTopicRoomIfFirstToday(roomId)) {
            return;
        }
        for (Long memberId : memberIds) {
            notificationPublisher.publish(NotificationEvent.hotTopicRoom(memberId, roomId, roomName));
        }
    }
}
