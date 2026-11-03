package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.FeaturedNotificationDedupAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.event.NotificationPushEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeaturedNotificationHelper {

    private final FeaturedNotificationDedupAdaptor dedupAdaptor;
    private final NotificationPublisher notificationPublisher;
    private final NotificationAdaptor notificationAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;
    private final UserAdaptor userAdaptor;

    // 오늘 처음이면 표시하고 true
    public boolean markTodayFeedIfFirst(Long feedId) {
        return dedupAdaptor.markFeedIfFirstToday(feedId);
    }

    public void unmarkTodayFeed(Long feedId) {
        dedupAdaptor.unmarkFeedToday(feedId);
    }

    // 알림 리스너가 커밋 뒤에 돌아서 트랜잭션이 있어야 발행된다
    @Transactional
    public void notifyTodayFeed(Long feedId, Long authorUserId) {
        notificationPublisher.publish(NotificationEvent.todayFeed(authorUserId, feedId));
    }

    // 보냄 표시와 앱 안 알림을 같이 커밋하고 푸시만 커밋 뒤에 보낸다. 실패하면 표시까지 롤백
    @Transactional
    public boolean notifyHotTopicRoomIfDue(Long roomId, String roomName, List<Long> memberIds,
                                           LocalDateTime now, LocalDateTime renotifyBefore) {
        if (!topicRoomAdaptor.markHotNotifiedIfDue(roomId, now, renotifyBefore)) {
            return false;
        }

        List<NotificationEvent> events = userAdaptor.findUsersByIds(memberIds).stream()
                .filter(user -> user.getAccountState() != AccountState.DELETED)
                .map(user -> NotificationEvent.hotTopicRoom(user.getId(), roomId, roomName))
                .toList();
        List<Notification> saved = notificationAdaptor.saveAll(events.stream().map(Notification::from).toList());
        for (int i = 0; i < events.size(); i++) {
            notificationPublisher.publishPushOrThrow(new NotificationPushEvent(saved.get(i).getId(), events.get(i)));
        }
        return true;
    }
}
