package com.storix.batch.scheduler;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.service.AdminNotificationBroadcastService;
import com.storix.domain.domains.notification.service.AdminNotificationLifecycleService;
import com.storix.infrastructure.external.notification.dispatcher.AdminNotificationRetryer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationScheduler {

    private static final int STALE_MINUTES = 30;
    private static final int RESUME_GIVEUP_HOURS = 2;
    private static final int SENDING_LOG_STALE_MINUTES = 15;

    private static final String MDC_KEY = STORIXStatic.Mdc.ADMIN_NOTIFICATION_ID;

    private final AdminNotificationLifecycleService adminNotificationLifecycleService;
    private final AdminNotificationBroadcastService adminNotificationBroadcastService;
    private final AdminNotificationRetryer adminNotificationRetryer;

    // 예약 시각이 된 발송 실행
    @Scheduled(cron = "0 0/5 * * * *", zone = "Asia/Seoul")
    public void broadcastDueNotifications() {
        adminNotificationLifecycleService.findDueNotifications(LocalDateTime.now())
                .forEach(notification -> {
                    log.info(">>> [AdminNotificationScheduler] broadcast 예약 발송 id={}", notification.getId());
                    adminNotificationBroadcastService.broadcast(notification.getId());
                });
    }

    // 비동기 발송 상태 보정
    @Scheduled(cron = "0 3/5 * * * *", zone = "Asia/Seoul")
    public void reconcileSendingNotifications() {

        LocalDateTime now = LocalDateTime.now();

        // 0. 전송 중(SENDING)으로 멈춘 로그 회수
        int recoveredLogs = adminNotificationLifecycleService.resetStaleSendingLogs(now.minusMinutes(SENDING_LOG_STALE_MINUTES));

        // 1. 전체 청크 발행 완료이지만, SENDING 인 발송 종료
        List<Long> completable = adminNotificationLifecycleService.findCompletableSendingIds();
        reconcileEach(completable, "완료 종료", id -> withMdc(id, adminNotificationLifecycleService::tryFinalize));

        // 2. 2시간 넘게 진행 없는 발행 중단 건 강제 마감
        List<Long> abandoned = adminNotificationLifecycleService.findStaleIncompleteSendingIds(now.minusHours(RESUME_GIVEUP_HOURS));
        reconcileEach(abandoned, "발행 중단 강제 마감", id -> withMdc(id, adminNotificationLifecycleService::forceFinalize));

        // 3. 발행 도중 멈춘 발송 -> 커서부터 재개
        LocalDateTime resumeCutoff = now.minusMinutes(STALE_MINUTES);
        List<Long> resumable = adminNotificationLifecycleService.findStaleIncompleteSendingIds(resumeCutoff);
        reconcileEach(resumable, "재개", id -> adminNotificationBroadcastService.resumeBroadcast(id, resumeCutoff));

        // 4. 발행은 끝났는데 멈춘 발송 -> 강제 종료
        List<Long> stale = adminNotificationLifecycleService.findStaleCompletedSendingIds(now.minusMinutes(STALE_MINUTES));
        reconcileEach(stale, "완료 정체 강제 종료", id -> withMdc(id, adminNotificationLifecycleService::forceFinalize));

        if (recoveredLogs > 0 || !completable.isEmpty() || !abandoned.isEmpty() || !resumable.isEmpty() || !stale.isEmpty()) {
            log.info(">>> [AdminNotificationScheduler] 상태 보정 recoveredLogs={} completable={} abandoned={} resumed={} stale={}",
                    recoveredLogs, completable.size(), abandoned.size(), resumable.size(), stale.size());
        }
    }

    // 미발송 로그 지연 재시도
    @Scheduled(cron = "0 2/5 * * * *", zone = "Asia/Seoul")
    public void retryFailedNotificationLogs() {
        int retried = adminNotificationRetryer.retryDueLogs(LocalDateTime.now());
        if (retried > 0) {
            log.info(">>> [AdminNotificationScheduler] 재시도 logs={}", retried);
        }
    }

    // 항목별 예외 개별 로깅
    private void reconcileEach(List<Long> ids, String stage, Consumer<Long> action) {
        for (Long id : ids) {
            try {
                action.accept(id);
            } catch (Exception e) {
                log.error(">>> [AdminNotificationScheduler] 보정 실패 stage={} id={}, cause={}", stage, id, e.getMessage(), e);
            }
        }
    }

    // 처리 건마다 MDC 설정
    private void withMdc(Long adminNotificationId, Consumer<Long> action) {
        MDC.put(MDC_KEY, String.valueOf(adminNotificationId));
        try {
            action.accept(adminNotificationId);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
