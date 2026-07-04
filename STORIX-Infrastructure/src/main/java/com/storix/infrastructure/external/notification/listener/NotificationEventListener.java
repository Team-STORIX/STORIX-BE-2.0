package com.storix.infrastructure.external.notification.listener;

import com.storix.domain.domains.notification.dto.DispatchResult;
import com.storix.domain.domains.notification.event.NotificationEvent;
import com.storix.domain.domains.notification.service.NotificationDispatchService;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import com.storix.infrastructure.external.notification.exception.FcmTransientException;
import com.storix.infrastructure.external.notification.fcm.FcmPushExecutor;
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

    private static final int MAX_SEND_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 500L;

    private final FcmPushExecutor fcmPushExecutor;
    private final NotificationDispatchService notificationDispatchService;

    @Async("notificationConsumerExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(NotificationEvent event) {
        try {
            // 1. Domain 비즈니스 로직 위임 (인앱 저장 + 푸시 발송 대상 결정)
            DispatchResult result = notificationDispatchService.dispatch(event);

            // 2. 푸시 발송 대상 없으면 종료
            if (!result.shouldSendPush()) return;

            // 3. FCM 멀티캐스트 발송 + 토큰 후처리 (일시오류 시 최대 3회 백오프 재시도)
            sendWithRetry(event, result);
        } catch (Exception e) {
            log.error(">>> [Notification] dispatch failed event={}, cause={}", event, e.getMessage(), e);
        }
    }

    // 일시 오류 재시도
    private void sendWithRetry(NotificationEvent event, DispatchResult result) {
        Map<String, String> data = buildData(event, result.notificationId());
        FcmTransientException lastTransient = null;
        for (int attempt = 1; attempt <= MAX_SEND_ATTEMPTS; attempt++) {
            try {
                MulticastResult mc = fcmPushExecutor.sendAndApply(result.tokens(), data);
                if (mc.hasTransientFailure()) {
                    log.warn(">>> [Notification] 일부 토큰 일시오류 - 재시도 생략(중복발송 방지) notificationId={}, failure={}",
                            result.notificationId(), mc.failureCount());
                }
                return;
            } catch (FcmTransientException e) {
                lastTransient = e;
                if (attempt < MAX_SEND_ATTEMPTS) {
                    log.warn(">>> [Notification] FCM 일시오류 재시도 {}/{} notificationId={}, code={}",
                            attempt, MAX_SEND_ATTEMPTS, result.notificationId(), e.getMessagingErrorCode());
                    sleep(BASE_BACKOFF_MS << (attempt - 1)); // 0.5s, 1s
                }
            } catch (Exception e) {
                log.error(">>> [Notification] FCM 영구 실패(재시도 안 함) notificationId={}, cause={}",
                        result.notificationId(), e.getMessage());
                return;
            }
        }

        log.error(">>> [Notification] FCM 일시오류 최종 실패 - 재시도 {}회 소진 notificationId={}, code={}",
                MAX_SEND_ATTEMPTS, result.notificationId(), lastTransient.getMessagingErrorCode());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
