package com.storix.infrastructure.external.notification.dispatcher;

import com.storix.domain.domains.notification.domain.AdminNotificationDeliveryOutcome;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import com.storix.domain.domains.notification.dto.AdminNotificationDispatchCounts;
import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.event.AdminNotificationChunkEvent;
import com.storix.domain.domains.notification.service.AdminNotificationDeliveryResultService;
import com.storix.common.utils.NightWindow;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.pushdevice.dto.ActivePushToken;
import com.storix.domain.domains.topicroom.dto.UserUnreadCount;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import com.storix.infrastructure.external.notification.exception.FcmTransientException;
import com.storix.infrastructure.external.notification.fcm.FcmPushExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationDispatcher {

    private static final int MAX_ATTEMPTS = 3;

    private final PushDeviceAdaptor pushDeviceAdaptor;
    private final FcmPushExecutor fcmPushExecutor;
    private final AdminNotificationDeliveryResultService deliveryResultService;
    private final NotificationAdaptor notificationAdaptor;
    private final NotificationSettingAdaptor notificationSettingAdaptor;
    private final ChatAdaptor chatAdaptor;

    // 대상 유저에게 발송하고 결과를 로그에 반영
    public AdminNotificationDispatchCounts dispatch(AdminNotificationChunkEvent event, LocalDateTime now) {
        List<Long> userIds = event.userIds();
        if (userIds.isEmpty()) return AdminNotificationDispatchCounts.empty();

        Long adminNotificationId = event.adminNotificationId();
        AdminNotificationTargetType targetType = event.targetType();
        Long eventTargetId = event.eventTargetId();
        String targetLink = event.targetLink();
        NotificationType notificationType = event.notificationType().getNotificationType();

        // 0. 야간 마케팅 발송 연기 - 실제 발송 시점이 야간이면 발송/인앱생성 없이 다음 08:00로 미룸
        if (event.isMarketing() && NightWindow.isNight(now)) {
            LocalDateTime deferUntil = NightWindow.nextAllowedAt(now);
            deliveryResultService.deferMarketingChunk(adminNotificationId, userIds, deferUntil);
            log.info(">>> [AdminNotification] 야간 마케팅 발송 연기 count={}, until={}", userIds.size(), deferUntil);
            return AdminNotificationDispatchCounts.empty();
        }

        // 1. 발송 대상 인앱 알림 생성
        Map<Long, Long> notificationIdByUser = deliveryResultService.prepareBroadcastNotifications(
                adminNotificationId, userIds, notificationType, targetType, eventTargetId, targetLink,
                event.title(), event.content());
        if (notificationIdByUser.isEmpty()) return AdminNotificationDispatchCounts.empty();

        // 2. 발송 대상 활성 토큰 조회 — 타입별 수신 동의한 유저만 (인앱 저장은 동의와 무관)
        List<Long> targets = List.copyOf(notificationIdByUser.keySet());
        // 뱃지는 알림함 + 토픽룸 채팅 미읽음 총합 — 다른 발송 경로와 같은 기준
        Map<Long, Integer> inboxUnread = notificationAdaptor.countUnreadByUserIds(targets);
        Map<Long, Long> chatUnread = chatAdaptor.countTotalUnreadByUserIds(targets).stream()
                .collect(Collectors.toMap(UserUnreadCount::userId, UserUnreadCount::unreadCount));
        List<Long> consented = notificationSettingAdaptor.findAllByUserIds(targets).stream()
                .filter(setting -> setting.acceptsType(notificationType))
                .map(NotificationSetting::getUserId)
                .toList();
        Map<Long, List<String>> tokensByUserId = pushDeviceAdaptor.findActiveTokensByUserIds(consented).stream()
                .collect(Collectors.groupingBy(
                        ActivePushToken::userId,
                        LinkedHashMap::new,
                        Collectors.mapping(ActivePushToken::fcmToken, Collectors.toList())
                ));

        // 3. 유저별 FCM 발송 -> 결과 분류
        Map<Long, AdminNotificationDeliveryOutcome> outcomes = new LinkedHashMap<>();
        for (Long userId : targets) {
            MDC.put(STORIXStatic.Mdc.RECIPIENT_USER_ID, String.valueOf(userId)); // FcmSender 로그까지 전파
            try {
                List<String> tokens = tokensByUserId.getOrDefault(userId, List.of());
                if (tokens.isEmpty()) {
                    outcomes.put(userId, AdminNotificationDeliveryOutcome.SKIPPED);
                    continue;
                }
                try {
                    MulticastResult result = fcmPushExecutor.sendAndApply(
                            tokens, buildData(notificationType, targetType, eventTargetId, targetLink,
                                    event.title(), event.content(), notificationIdByUser.get(userId),
                                    inboxUnread.getOrDefault(userId, 0)
                                            + chatUnread.getOrDefault(userId, 0L).intValue()));
                    if (!result.successTokens().isEmpty()) {
                        outcomes.put(userId, AdminNotificationDeliveryOutcome.SENT);
                    } else if (result.hasTransientFailure()) {
                        outcomes.put(userId, AdminNotificationDeliveryOutcome.TRANSIENT_FAILURE);
                    } else if (result.failureCount() > 0 && result.failureCount() == result.invalidTokens().size()) {
                        outcomes.put(userId, AdminNotificationDeliveryOutcome.SKIPPED);
                    } else {
                        outcomes.put(userId, AdminNotificationDeliveryOutcome.PERMANENT_FAILURE);
                    }
                } catch (FcmTransientException e) {
                    outcomes.put(userId, AdminNotificationDeliveryOutcome.TRANSIENT_FAILURE);
                    log.warn(">>> [AdminNotification] 푸시 일시 실패 code={}", e.getMessagingErrorCode());
                } catch (Exception e) {
                    outcomes.put(userId, AdminNotificationDeliveryOutcome.PERMANENT_FAILURE);
                    log.warn(">>> [AdminNotification] 푸시 영구 실패 cause={}", e.getMessage());
                }
            } finally {
                MDC.remove(STORIXStatic.Mdc.RECIPIENT_USER_ID);
            }
        }

        // 4. 결과 로그 반영
        return deliveryResultService.applyDispatchOutcomes(adminNotificationId, outcomes, MAX_ATTEMPTS, now);
    }

    private Map<String, String> buildData(NotificationType notificationType,
                                          AdminNotificationTargetType targetType, Long eventTargetId, String targetLink,
                                          String title, String content, Long notificationId, int unreadCount
    ) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(notificationId));
        data.put("type", notificationType.name());
        data.put("category", notificationType.category().name());
        data.put("unreadCount", String.valueOf(unreadCount));

        data.put("targetType", targetType.getTargetType().name());
        if (targetType == AdminNotificationTargetType.APP_EVENT && eventTargetId != null) {
            data.put("targetId", String.valueOf(eventTargetId));
        }
        if (targetType == AdminNotificationTargetType.EXTERNAL && targetLink != null) {
            data.put("targetLink", targetLink);
        }

        data.put("title", title);
        data.put("body", content);
        return data;
    }
}
