package com.storix.infrastructure.external.notification.dispatcher;

import com.storix.domain.domains.notification.domain.AdminNotificationDeliveryOutcome;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import com.storix.domain.domains.notification.dto.AdminNotificationDispatchCounts;
import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.NotificationType;
import com.storix.domain.domains.notification.service.AdminNotificationDeliveryResultService;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.pushdevice.dto.ActivePushToken;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import com.storix.infrastructure.external.notification.exception.FcmTransientException;
import com.storix.infrastructure.external.notification.fcm.FcmPushExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // 대상 유저에게 발송하고 결과를 로그에 반영
    public AdminNotificationDispatchCounts dispatch(Long adminNotificationId, String title, String content,
                                                   AdminNotificationType adminNotificationType, AdminNotificationTargetType targetType,
                                                   Long eventTargetId, String targetLink, List<Long> userIds, LocalDateTime now
    ) {
        if (userIds.isEmpty()) return AdminNotificationDispatchCounts.empty();

        // 마케팅의 경우만 푸시 알림 : (광고) ~~ (수신거부 : 설정), 인앱 : 원문
        boolean isMarketing = adminNotificationType == AdminNotificationType.MARKETING;
        NotificationType notificationType = adminNotificationType.getNotificationType();
        String pushTitle = isMarketing ? String.format(STORIXStatic.Notification.TITLE_MARKETING, title) : title;
        String pushContent = isMarketing ? String.format(STORIXStatic.Notification.TPL_MARKETING, content) : content;

        // 1. 발송 대상 인앱 알림 생성
        Map<Long, Long> notificationIdByUser = deliveryResultService.prepareBroadcastNotifications(
                adminNotificationId, userIds, notificationType, targetType, eventTargetId, targetLink, title, content);
        if (notificationIdByUser.isEmpty()) return AdminNotificationDispatchCounts.empty();

        // 2. 발송 대상 활성 토큰 조회
        List<Long> targets = List.copyOf(notificationIdByUser.keySet());
        List<ActivePushToken> activeTokens = isMarketing
                ? pushDeviceAdaptor.findMarketingEnabledActiveTokensByUserIds(targets) // 마케팅 알림일 경우, 동의자만
                : pushDeviceAdaptor.findActiveTokensByUserIds(targets);
        Map<Long, List<String>> tokensByUserId = activeTokens.stream()
                .collect(Collectors.groupingBy(
                        ActivePushToken::userId,
                        LinkedHashMap::new,
                        Collectors.mapping(ActivePushToken::fcmToken, Collectors.toList())
                ));

        // 3. 유저별 FCM 발송 -> 결과 분류
        Map<Long, AdminNotificationDeliveryOutcome> outcomes = new LinkedHashMap<>();
        for (Long userId : targets) {
            List<String> tokens = tokensByUserId.getOrDefault(userId, List.of());
            if (tokens.isEmpty()) {
                outcomes.put(userId, AdminNotificationDeliveryOutcome.SKIPPED);
                continue;
            }
            try {
                MulticastResult result = fcmPushExecutor.sendAndApply(
                        tokens, buildData(adminNotificationId, notificationType, targetType, eventTargetId, targetLink,
                                pushTitle, pushContent, notificationIdByUser.get(userId)));
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
                log.warn(">>> [AdminNotification] 푸시 일시 실패 adminNotificationId={}, userId={}, code={}",
                        adminNotificationId, userId, e.getMessagingErrorCode());
            } catch (Exception e) {
                outcomes.put(userId, AdminNotificationDeliveryOutcome.PERMANENT_FAILURE);
                log.warn(">>> [AdminNotification] 푸시 영구 실패 adminNotificationId={}, userId={}, cause={}",
                        adminNotificationId, userId, e.getMessage());
            }
        }

        // 4. 결과 로그 반영
        return deliveryResultService.applyDispatchOutcomes(adminNotificationId, outcomes, MAX_ATTEMPTS, now);
    }

    private Map<String, String> buildData(Long adminNotificationId, NotificationType notificationType,
                                          AdminNotificationTargetType targetType, Long eventTargetId, String targetLink,
                                          String title, String content, Long notificationId
    ) {
        Map<String, String> data = new HashMap<>();
        if (notificationId != null) {
            data.put("notificationId", String.valueOf(notificationId));
        }
        data.put("type", notificationType.name());
        data.put("category", notificationType.category().name());

        data.put("targetType", targetType.getTargetType().name());
        if (targetType == AdminNotificationTargetType.APP_EVENT && eventTargetId != null) {
            data.put("targetId", String.valueOf(eventTargetId));
        }
        if (targetType == AdminNotificationTargetType.EXTERNAL && targetLink != null) {
            data.put("link", targetLink);
        }

        data.put("title", title);
        data.put("body", content);
        data.put("adminNotificationId", String.valueOf(adminNotificationId));
        return data;
    }
}
