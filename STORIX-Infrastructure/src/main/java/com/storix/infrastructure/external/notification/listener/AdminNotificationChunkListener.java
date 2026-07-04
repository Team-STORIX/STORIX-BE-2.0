package com.storix.infrastructure.external.notification.listener;

import com.storix.domain.domains.notification.dto.AdminNotificationDispatchCounts;
import com.storix.domain.domains.notification.event.AdminNotificationChunkEvent;
import com.storix.domain.domains.notification.service.AdminNotificationDeliveryResultService;
import com.storix.infrastructure.external.notification.dispatcher.AdminNotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationChunkListener {

    private final AdminNotificationDeliveryResultService deliveryResultService;
    private final AdminNotificationDispatcher adminNotificationDispatcher;

    @Async("adminNotificationConsumerExecutor")
    @EventListener
    public void onChunk(AdminNotificationChunkEvent event) {
        Long adminNotificationId = event.adminNotificationId();
        try {
            AdminNotificationDispatchCounts result = adminNotificationDispatcher.dispatch(
                    adminNotificationId, event.title(), event.content(), event.notificationType(),
                    event.targetType(), event.eventTargetId(), event.targetLink(),
                    event.userIds(), LocalDateTime.now());

            deliveryResultService.accumulateProgress(adminNotificationId, result.sent(), result.failed(), result.skipped());

            log.info(">>> [AdminNotification] chunk 처리 완료 adminNotificationId={} userIds={} sent={} failed={} skipped={}",
                    adminNotificationId, event.userIds().size(), result.sent(), result.failed(), result.skipped());
        } catch (Exception e) {
            // 청크 실패 시 PENDING 으로 남겨 재시도 대상으로 두고 계속 진행
            log.error(">>> [AdminNotification] chunk 실패 cause={}", e.getMessage(), e);
        }
    }
}
