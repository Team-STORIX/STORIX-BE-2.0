package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.FeaturedNotificationDedupAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.event.NotificationPushEvent;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[HOT 토픽룸 알림] 보냄 표시 + 앱 안 알림 저장 + 푸시 발행")
class FeaturedNotificationHelperTest {

    private static final Long ROOM_ID = 10L;
    private static final String ROOM_NAME = "방";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 3, 12, 5);
    private static final LocalDateTime RENOTIFY_BEFORE = NOW.minusDays(7);

    @Mock
    private FeaturedNotificationDedupAdaptor dedupAdaptor;
    @Mock
    private NotificationPublisher notificationPublisher;
    @Mock
    private NotificationAdaptor notificationAdaptor;
    @Mock
    private TopicRoomAdaptor topicRoomAdaptor;
    @Mock
    private UserAdaptor userAdaptor;

    private FeaturedNotificationHelper helper;

    @BeforeEach
    void setUp() {
        helper = new FeaturedNotificationHelper(
                dedupAdaptor, notificationPublisher, notificationAdaptor, topicRoomAdaptor, userAdaptor);
    }

    @Test
    @DisplayName("보낼 차례가 아니면 저장 · 발행 없이 false")
    void notDue() {
        given(topicRoomAdaptor.markHotNotifiedIfDue(ROOM_ID, NOW, RENOTIFY_BEFORE)).willReturn(false);

        boolean notified = helper.notifyHotTopicRoomIfDue(ROOM_ID, ROOM_NAME, List.of(1L, 2L), NOW, RENOTIFY_BEFORE);

        assertThat(notified).isFalse();
        verify(notificationAdaptor, never()).saveAll(anyList());
        verify(notificationPublisher, never()).publishPushOrThrow(any());
    }

    @Test
    @DisplayName("탈퇴 유저는 빼고 저장한 뒤 저장된 알림 id 로 푸시 이벤트를 발행")
    void savesAndPublishesPush() {
        given(topicRoomAdaptor.markHotNotifiedIfDue(ROOM_ID, NOW, RENOTIFY_BEFORE)).willReturn(true);
        List<User> users = List.of(
                user(1L, AccountState.NORMAL), user(2L, AccountState.DELETED), user(3L, AccountState.SUSPENDED));
        given(userAdaptor.findUsersByIds(List.of(1L, 2L, 3L))).willReturn(users);
        given(notificationAdaptor.saveAll(anyList())).willAnswer(invocation -> {
            List<Notification> notifications = invocation.getArgument(0);
            for (int i = 0; i < notifications.size(); i++) {
                ReflectionTestUtils.setField(notifications.get(i), "id", 100L + i);
            }
            return notifications;
        });

        boolean notified = helper.notifyHotTopicRoomIfDue(ROOM_ID, ROOM_NAME, List.of(1L, 2L, 3L), NOW, RENOTIFY_BEFORE);

        assertThat(notified).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationAdaptor).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue())
                .extracting(Notification::getUserId, Notification::getNotificationType, Notification::getTargetId)
                .containsExactly(
                        tuple(1L, NotificationType.HOT_TOPIC_ROOM, ROOM_ID),
                        tuple(3L, NotificationType.HOT_TOPIC_ROOM, ROOM_ID));

        ArgumentCaptor<NotificationPushEvent> pushCaptor = ArgumentCaptor.forClass(NotificationPushEvent.class);
        verify(notificationPublisher, times(2)).publishPushOrThrow(pushCaptor.capture());
        assertThat(pushCaptor.getAllValues())
                .extracting(NotificationPushEvent::notificationId, push -> push.event().recipientUserId())
                .containsExactly(
                        tuple(100L, 1L),
                        tuple(101L, 3L));
    }

    @Test
    @DisplayName("저장이 실패하면 예외가 그대로 나가 트랜잭션이 롤백되고 푸시도 발행하지 않는다")
    void saveFailurePropagates() {
        given(topicRoomAdaptor.markHotNotifiedIfDue(ROOM_ID, NOW, RENOTIFY_BEFORE)).willReturn(true);
        List<User> users = List.of(user(1L, AccountState.NORMAL));
        given(userAdaptor.findUsersByIds(List.of(1L))).willReturn(users);
        willThrow(new IllegalStateException("db down")).given(notificationAdaptor).saveAll(anyList());

        assertThatThrownBy(() -> helper.notifyHotTopicRoomIfDue(ROOM_ID, ROOM_NAME, List.of(1L), NOW, RENOTIFY_BEFORE))
                .isInstanceOf(IllegalStateException.class);
        verify(notificationPublisher, never()).publishPushOrThrow(any());
    }

    private User user(Long id, AccountState state) {
        User user = mock(User.class);
        given(user.getAccountState()).willReturn(state);
        if (state != AccountState.DELETED) {
            given(user.getId()).willReturn(id);
        }
        return user;
    }
}
