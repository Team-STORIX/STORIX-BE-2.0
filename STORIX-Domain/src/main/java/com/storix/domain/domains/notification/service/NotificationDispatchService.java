package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.notification.dto.DispatchResult;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.domain.AccountState;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final UserAdaptor userAdaptor;
    private final UserBlockAdaptor userBlockAdaptor;
    private final NotificationAdaptor notificationAdaptor;
    private final NotificationSettingAdaptor notificationSettingAdaptor;

    private final PushDeviceAdaptor pushDeviceAdaptor;

    @Transactional
    public DispatchResult dispatch(NotificationEvent event) {
        // 1. 탈퇴 유저는 전부 skip
        User user = userAdaptor.findUserById(event.recipientUserId());
        if (user.getAccountState() == AccountState.DELETED) {
            log.debug(">>> [Notification] skipped (user deleted) userId={}", event.recipientUserId());
            return DispatchResult.skip();
        }

        // 1-2. 수신자가 행위자를 차단했으면 전부 skip > 인앱 저장, 푸시 알림 X
        if (event.actorUserId() != null
                && userBlockAdaptor.isBlocked(event.recipientUserId(), event.actorUserId())) {
            log.debug(">>> [Notification] skipped (actor blocked) userId={}, actorId={}",
                    event.recipientUserId(), event.actorUserId());
            return DispatchResult.skip();
        }

        // 2. 인앱 알림함 저장 > 수신 동의와 무관, 항상 저장
        Notification saved = notificationAdaptor.save(Notification.builder()
                .userId(event.recipientUserId())
                .notificationType(event.notificationType())
                .targetType(event.targetType())
                .targetId(event.targetId())
                .parentTargetId(event.parentTargetId())
                .title(event.title())
                .content(event.content())
                .build());

        // 3. 정지 유저는 제재/신고 안내 타입만 푸시 허용
        if (user.getAccountState() == AccountState.SUSPENDED
                && !event.notificationType().deliverableToSuspendedUser()) {
            log.debug(">>> [Notification] push skipped (user suspended) userId={}, type={}",
                    event.recipientUserId(), event.notificationType());
            return DispatchResult.inAppOnly(saved.getId());
        }

        // 4. 푸시 수신 동의 체크
        NotificationSetting setting = notificationSettingAdaptor.getByUserId(event.recipientUserId());
        if (!setting.acceptsType(event.notificationType())) {
            log.debug(">>> [Notification] push skipped (type disabled) userId={}, type={}",
                    event.recipientUserId(), event.notificationType());
            return DispatchResult.inAppOnly(saved.getId());
        }

        // 5. 푸시 대상 확정 — 활성 디바이스 토큰 조회 > 없으면 인앱만
        List<String> tokens = pushDeviceAdaptor.findActiveFcmTokensByUserId(event.recipientUserId());
        if (tokens.isEmpty()) {
            log.debug(">>> [Notification] no active device for userId={}", event.recipientUserId());
            return DispatchResult.inAppOnly(saved.getId());
        }

        // 뱃지 표시용 미읽음 총합 — 방금 저장한 알림 포함
        int unreadCount = notificationAdaptor.countUnreadByUserId(event.recipientUserId());
        return DispatchResult.pushTo(saved.getId(), tokens, unreadCount);
    }
}
