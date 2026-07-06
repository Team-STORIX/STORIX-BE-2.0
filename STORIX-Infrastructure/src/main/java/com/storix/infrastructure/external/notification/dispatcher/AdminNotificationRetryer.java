package com.storix.infrastructure.external.notification.dispatcher;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;
import com.storix.domain.domains.notification.service.AdminNotificationLifecycleService;
import com.storix.domain.domains.notification.service.AdminNotificationTargetService;
import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationRetryer {

    private static final int RETRY_BATCH_SIZE = 500;
    private static final String MDC_KEY = STORIXStatic.Mdc.ADMIN_NOTIFICATION_ID;

    private final AdminNotificationTargetService targetService;
    private final AdminNotificationLifecycleService lifecycleService;
    private final AdminNotificationDispatcher adminNotificationDispatcher;

    public int retryDueLogs(LocalDateTime now) {

        // 1. 재시도 시각이 된 PENDING 로그 조회
        List<AdminNotificationLog> due = lifecycleService.findDueRetryable(now, RETRY_BATCH_SIZE);
        if (due.isEmpty()) return 0;

        // 2. 이벤트별로 유저를 묶어 title/content 한 번만 조회 후 재발송
        Map<Long, List<Long>> userIdsByEvent = due.stream()
                .collect(Collectors.groupingBy(
                        AdminNotificationLog::getAdminNotificationId,
                        Collectors.mapping(AdminNotificationLog::getUserId, Collectors.toList())));

        for (Map.Entry<Long, List<Long>> entry : userIdsByEvent.entrySet()) {
            Long adminNotificationId = entry.getKey();
            MDC.put(MDC_KEY, String.valueOf(adminNotificationId)); // 이벤트 단위 로그 상관키

            try {
                AdminNotificationBroadcastInfo info = targetService.getBroadcastInfo(adminNotificationId);
                adminNotificationDispatcher.dispatch(
                        adminNotificationId, info.title(), info.content(), info.notificationType(),
                        info.targetType(), info.eventTargetId(), info.targetLink(),
                        entry.getValue(), now);

                // [AdminNotification] updatedAt 갱신
                lifecycleService.touchProgress(adminNotificationId);

                // 마지막 PENDING 로그 까지 처리됐으면 완료 종료
                lifecycleService.tryFinalize(adminNotificationId);
            } catch (Exception e) {
                log.error(">>> [AdminNotification] 재시도 발송 실패 adminNotificationId={}, cause={}", adminNotificationId, e.getMessage(), e);
            } finally {
                MDC.remove(MDC_KEY);
            }
        }

        log.info(">>> [AdminNotification] 재시도 발송 logs={} events={}", due.size(), userIdsByEvent.size());
        return due.size();
    }
}
