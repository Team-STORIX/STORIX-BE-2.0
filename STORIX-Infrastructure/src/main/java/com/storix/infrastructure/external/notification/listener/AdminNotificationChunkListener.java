package com.storix.infrastructure.external.notification.listener;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.dto.AdminNotificationDispatchCounts;
import com.storix.domain.domains.notification.event.AdminNotificationChunkEvent;
import com.storix.domain.domains.notification.service.AdminNotificationDeliveryResultService;
import com.storix.infrastructure.external.notification.dispatcher.AdminNotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationChunkListener {

    private static final String MDC_KEY = STORIXStatic.Mdc.ADMIN_NOTIFICATION_ID;

    private final AdminNotificationDeliveryResultService deliveryResultService;
    private final AdminNotificationDispatcher adminNotificationDispatcher;

    @Async("adminNotificationConsumerExecutor")
    @EventListener
    public void onChunk(AdminNotificationChunkEvent event) {
        Long adminNotificationId = event.adminNotificationId();
        MDC.put(MDC_KEY, String.valueOf(adminNotificationId));
        try {
            AdminNotificationDispatchCounts result = adminNotificationDispatcher.dispatch(event, LocalDateTime.now());

            deliveryResultService.accumulateProgress(adminNotificationId, result.sent(), result.failed(), result.skipped());

            log.info(">>> [AdminNotification] chunk 처리 완료 adminNotificationId={} userIds={} sent={} failed={} skipped={}",
                    adminNotificationId, event.userIds().size(), result.sent(), result.failed(), result.skipped());
        } catch (Exception e) {
            // 청크 실패 시 PENDING 으로 남겨 재시도 대상으로 두고 계속 진행
            log.error(">>> [AdminNotification] chunk 실패 adminNotificationId={}, cause={}", adminNotificationId, e.getMessage(), e);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
