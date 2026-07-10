package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.AdminNotificationLogAdaptor;
import com.storix.domain.domains.notification.adaptor.NotificationAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotificationDeliveryOutcome;
import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.dto.AdminNotificationDispatchCounts;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.domain.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationDeliveryResultService {

    private final AdminNotificationAdaptor adminNotificationAdaptor;
    private final AdminNotificationLogAdaptor adminNotificationLogAdaptor;
    private final NotificationAdaptor notificationAdaptor;
    private final AdminNotificationLifecycleService lifecycleService;

    // FCM 발송 전 발송 로그 선점 + 인앱 알림 생성
    @Transactional
    public Map<Long, Long> prepareBroadcastNotifications(Long adminNotificationId, List<Long> userIds,
                                                         NotificationType notificationType, AdminNotificationTargetType targetType,
                                                         Long eventTargetId, String targetLink, String title, String content
    ) {
        // 1. 발송 대기 로그를 잠가 발송 중으로 선점 (SKIP LOCKED)
        List<AdminNotificationLog> claimed = adminNotificationLogAdaptor.lockClaimablePending(adminNotificationId, userIds);
        if (claimed.isEmpty()) return Map.of();
        claimed.forEach(AdminNotificationLog::markSending);

        // 2. 인앱 알림 없는 선점 로그만 생성해 연결
        List<AdminNotificationLog> needNotification = claimed.stream()
                .filter(l -> l.getNotificationId() == null)
                .toList();
        if (!needNotification.isEmpty()) {
            List<Notification> saved = notificationAdaptor.saveAll(needNotification.stream()
                    .map(l -> switch (targetType) {
                        case NONE -> Notification.ofBroadcast(l.getUserId(), notificationType, title, content);
                        case APP_EVENT -> Notification.ofEventBroadcast(l.getUserId(), notificationType, title, content, eventTargetId);
                        case EXTERNAL -> Notification.ofExternalBroadcast(l.getUserId(), notificationType, title, content, targetLink);
                    })
                    .toList());
            for (int i = 0; i < needNotification.size(); i++) {
                needNotification.get(i).assignNotification(saved.get(i).getId());
            }
        }

        Map<Long, Long> notificationIdByUser = new LinkedHashMap<>();
        claimed.forEach(l -> notificationIdByUser.put(l.getUserId(), l.getNotificationId()));
        return notificationIdByUser;
    }

    // FCM 발송 결과 반영
    @Transactional
    public AdminNotificationDispatchCounts applyDispatchOutcomes(Long adminNotificationId,
                                                                Map<Long, AdminNotificationDeliveryOutcome> outcomes,
                                                                int maxAttempts, LocalDateTime now
    ) {
        if (outcomes.isEmpty()) return AdminNotificationDispatchCounts.empty();

        // 0. FCM 발송 결과 정리
        Map<AdminNotificationDeliveryOutcome, List<Long>> byOutcome = outcomes.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        List<Long> sentUsers = byOutcome.getOrDefault(AdminNotificationDeliveryOutcome.SENT, List.of());
        List<Long> skippedUsers = byOutcome.getOrDefault(AdminNotificationDeliveryOutcome.SKIPPED, List.of());
        List<Long> permanentUsers = byOutcome.getOrDefault(AdminNotificationDeliveryOutcome.PERMANENT_FAILURE, List.of());
        List<Long> transientUsers = byOutcome.getOrDefault(AdminNotificationDeliveryOutcome.TRANSIENT_FAILURE, List.of());

        // 1. SENT(성공)/SKIPPED(스킵)/PERMANENT_FAILURE(영구 실패)는 벌크 처리
        if (!sentUsers.isEmpty()) adminNotificationLogAdaptor.markSent(adminNotificationId, sentUsers, now);
        if (!skippedUsers.isEmpty()) adminNotificationLogAdaptor.markSkipped(adminNotificationId, skippedUsers);
        if (!permanentUsers.isEmpty()) adminNotificationLogAdaptor.markPermanentFailed(adminNotificationId, permanentUsers);

        // 2. TRANSIENT_FAILURE(일시 실패)는 행마다 지수 백오프가 다르므로 엔티티로 처리
        if (!transientUsers.isEmpty()) {
            adminNotificationLogAdaptor.findByChunk(adminNotificationId, transientUsers)
                    .forEach(log -> log.recordTransientFailure(maxAttempts, now));
        }

        // 3. 진행 집계 반환
        return new AdminNotificationDispatchCounts(sentUsers.size(), permanentUsers.size() + transientUsers.size(), skippedUsers.size());
    }

    // FCM 발송 결과 누적 후 완료 시도
    @Transactional
    public void accumulateProgress(Long adminNotificationId, int sent, int failed, int skipped) {
        adminNotificationAdaptor.addCounts(adminNotificationId, sent, failed, skipped, LocalDateTime.now());
        lifecycleService.tryFinalize(adminNotificationId);
    }

    // 야간 마케팅 청크 발송 연기 - 발송/집계 없이 다음 08:00로 재시도 예약
    @Transactional
    public void deferMarketingChunk(Long adminNotificationId, List<Long> userIds, LocalDateTime deferUntil) {
        adminNotificationLogAdaptor.deferPending(adminNotificationId, userIds, deferUntil);
    }
}
