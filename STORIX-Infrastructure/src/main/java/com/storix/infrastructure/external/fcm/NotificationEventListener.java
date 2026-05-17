package com.storix.infrastructure.external.fcm;

import com.storix.domain.domains.notification.dto.DispatchResult;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.service.NotificationDispatchService;
import com.storix.domain.domains.pushdevice.service.PushDispatchService;
import com.storix.infrastructure.external.fcm.dto.MulticastResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final FcmSender fcmSender;

    private final NotificationDispatchService notificationDispatchService;
    private final PushDispatchService pushDispatchService;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(NotificationEvent event) {
        try {
            // 1. Domain 비즈니스 로직 위임 (인앱 저장 + 푸시 발송 대상 결정)
            DispatchResult result = notificationDispatchService.dispatch(event);

            // 2. 푸시 발송 대상 없으면 종료
            if (!result.shouldSendPush()) return;

            // 3. FCM 멀티캐스트 발송
            MulticastResult mc = fcmSender.sendMulticast(
                    result.tokens(),
                    buildData(event, result.notificationId()));

            // 4. 토큰 후처리 (invalid 비활성화 + success lastSuccessAt 갱신)
            if (!mc.invalidTokens().isEmpty()) {
                pushDispatchService.deactivateTokens(mc.invalidTokens());
            }
            if (!mc.successTokens().isEmpty()) {
                pushDispatchService.markTokensSuccess(mc.successTokens());
            }
        } catch (Exception e) {
            log.error(">>> [Notification] dispatch failed event={}, cause={}", event, e.getMessage(), e);
        }
    }

    // FCM data payload 빌드
    private Map<String, String> buildData(NotificationEvent event, Long notificationId) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(notificationId));
        data.put("type", event.notificationType().name());
        data.put("category", event.notificationType().category().name());
        data.put("targetType", event.targetType().name());
        data.put("title", event.title());
        data.put("body", event.content());
        if (event.targetId() != null) {
            data.put("targetId", String.valueOf(event.targetId()));
        }
        if (event.parentTargetId() != null) {
            data.put("parentTargetId", String.valueOf(event.parentTargetId()));
        }
        return data;
    }
}
