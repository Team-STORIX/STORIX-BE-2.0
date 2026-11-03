package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.dto.DispatchResult;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.event.NotificationPushEvent;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("[알림 디스패치] 앱 안 저장 · 푸시 대상 결정")
class NotificationDispatchServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ACTOR_ID = 2L;
    private static final Long NOTIFICATION_ID = 100L;

    @Mock
    private UserAdaptor userAdaptor;
    @Mock
    private UserBlockAdaptor userBlockAdaptor;
    @Mock
    private ChatAdaptor chatAdaptor;
    @Mock
    private NotificationAdaptor notificationAdaptor;
    @Mock
    private NotificationSettingAdaptor notificationSettingAdaptor;
    @Mock
    private PushDeviceAdaptor pushDeviceAdaptor;

    private NotificationDispatchService service;

    @BeforeEach
    void setUp() {
        service = new NotificationDispatchService(userAdaptor, userBlockAdaptor, chatAdaptor,
                notificationAdaptor, notificationSettingAdaptor, pushDeviceAdaptor);
    }

    @Nested
    @DisplayName("dispatch: 저장 + 푸시 대상")
    class Dispatch {

        private final NotificationEvent event = NotificationEvent.likeFeed(USER_ID, ACTOR_ID, 5L, "닉네임");

        @Test
        @DisplayName("탈퇴 유저는 저장하지 않는다")
        void deletedUser() {
            givenUser(AccountState.DELETED);

            DispatchResult result = service.dispatch(event);

            assertThat(result.notificationId()).isNull();
            assertThat(result.shouldSendPush()).isFalse();
            verify(notificationAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("행위자를 차단했으면 저장하지 않는다")
        void blockedActor() {
            givenUser(AccountState.NORMAL);
            given(userBlockAdaptor.isBlocked(USER_ID, ACTOR_ID)).willReturn(true);

            DispatchResult result = service.dispatch(event);

            assertThat(result.notificationId()).isNull();
            verify(notificationAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("저장하고 동의 · 활성 토큰이 있으면 푸시 대상")
        void savesAndPushes() {
            givenUser(AccountState.NORMAL);
            givenSaved();
            givenConsent(event.notificationType(), true);
            given(pushDeviceAdaptor.findActiveFcmTokensByUserId(USER_ID)).willReturn(List.of("token"));
            given(notificationAdaptor.countUnreadByUserId(USER_ID)).willReturn(3);
            given(chatAdaptor.countTotalUnread(USER_ID)).willReturn(2L);

            DispatchResult result = service.dispatch(event);

            assertThat(result.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(result.shouldSendPush()).isTrue();
            assertThat(result.tokens()).containsExactly("token");
            assertThat(result.unreadCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("정지 유저의 일반 알림은 저장만")
        void suspendedUser() {
            givenUser(AccountState.SUSPENDED);
            givenSaved();

            DispatchResult result = service.dispatch(event);

            assertThat(result.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(result.shouldSendPush()).isFalse();
        }
    }

    @Nested
    @DisplayName("dispatchPush: 이미 저장된 알림의 푸시 대상만")
    class DispatchPush {

        private final NotificationEvent event = NotificationEvent.hotTopicRoom(USER_ID, 10L, "방");
        private final NotificationPushEvent pushEvent = new NotificationPushEvent(NOTIFICATION_ID, event);

        @Test
        @DisplayName("저장하지 않고 저장된 알림 id 로 푸시 대상을 정한다")
        void pushesWithoutSaving() {
            givenUser(AccountState.NORMAL);
            givenConsent(NotificationType.HOT_TOPIC_ROOM, true);
            given(pushDeviceAdaptor.findActiveFcmTokensByUserId(USER_ID)).willReturn(List.of("token"));
            given(notificationAdaptor.countUnreadByUserId(USER_ID)).willReturn(1);
            given(chatAdaptor.countTotalUnread(USER_ID)).willReturn(0L);

            DispatchResult result = service.dispatchPush(pushEvent);

            assertThat(result.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(result.shouldSendPush()).isTrue();
            assertThat(result.unreadCount()).isEqualTo(1);
            verify(notificationAdaptor, never()).save(any());
        }

        @Test
        @DisplayName("그 사이 탈퇴했으면 보내지 않는다")
        void deletedUser() {
            givenUser(AccountState.DELETED);

            DispatchResult result = service.dispatchPush(pushEvent);

            assertThat(result.shouldSendPush()).isFalse();
        }

        @Test
        @DisplayName("정지 유저는 보내지 않는다")
        void suspendedUser() {
            givenUser(AccountState.SUSPENDED);

            DispatchResult result = service.dispatchPush(pushEvent);

            assertThat(result.notificationId()).isEqualTo(NOTIFICATION_ID);
            assertThat(result.shouldSendPush()).isFalse();
        }

        @Test
        @DisplayName("수신 동의를 껐으면 보내지 않는다")
        void consentOff() {
            givenUser(AccountState.NORMAL);
            givenConsent(NotificationType.HOT_TOPIC_ROOM, false);

            DispatchResult result = service.dispatchPush(pushEvent);

            assertThat(result.shouldSendPush()).isFalse();
            verify(pushDeviceAdaptor, never()).findActiveFcmTokensByUserId(any());
        }

        @Test
        @DisplayName("활성 토큰이 없으면 보내지 않는다")
        void noToken() {
            givenUser(AccountState.NORMAL);
            givenConsent(NotificationType.HOT_TOPIC_ROOM, true);
            given(pushDeviceAdaptor.findActiveFcmTokensByUserId(USER_ID)).willReturn(List.of());

            DispatchResult result = service.dispatchPush(pushEvent);

            assertThat(result.shouldSendPush()).isFalse();
        }
    }

    private void givenUser(AccountState state) {
        User user = mock(User.class);
        given(user.getAccountState()).willReturn(state);
        given(userAdaptor.findUserById(USER_ID)).willReturn(user);
    }

    private void givenSaved() {
        given(notificationAdaptor.save(any())).willAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);
            return notification;
        });
    }

    private void givenConsent(NotificationType type, boolean accepts) {
        NotificationSetting setting = mock(NotificationSetting.class);
        given(setting.acceptsType(type)).willReturn(accepts);
        given(notificationSettingAdaptor.getByUserId(USER_ID)).willReturn(setting);
    }
}
