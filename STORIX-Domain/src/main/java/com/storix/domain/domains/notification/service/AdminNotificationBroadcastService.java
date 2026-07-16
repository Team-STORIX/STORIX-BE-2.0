package com.storix.domain.domains.notification.service;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;
import com.storix.domain.domains.notification.dto.AdminNotificationStartResult;
import com.storix.domain.domains.notification.event.AdminNotificationChunkEvent;
import com.storix.domain.domains.notification.publisher.AdminNotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationBroadcastService {

    private static final int TARGET_USER_CHUNK_SIZE = 1000;
    private static final long RELAY_GRACE_MINUTES = 5;
    private static final String MDC_KEY = STORIXStatic.Mdc.ADMIN_NOTIFICATION_ID;

    private final AdminNotificationTargetService targetService;
    private final AdminNotificationLifecycleService lifecycleService;
    private final AdminNotificationPublisher adminNotificationPublisher;

    // 1. 발송 실행 (즉시 발송 | 예약 발송 | 수동 재발송)
    @Async("adminNotificationProducerExecutor")
    public void broadcast(Long adminNotificationId) {
        MDC.put(MDC_KEY, String.valueOf(adminNotificationId));

        try {
            // 발송 가능 상태로 변경
            AdminNotificationStartResult result = lifecycleService.startSending(adminNotificationId);
            if (!result.started()) {
                log.info(">>> [AdminNotification] broadcast 건너뜀 - 발송 처리 불가 상태 status={}", result.status());
                return;
            }
            log.info(">>> [AdminNotification] broadcast 시작");
            runFanOut(adminNotificationId);
        } catch (Exception e) {
            // 발행 실패 시 SENDING 으로 남고 보정 스케줄러가 재개/종료
            log.error(">>> [AdminNotification] broadcast 발행 실패 cause={}", e.getMessage(), e);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    // 2. 중단된 발송 재개
    @Async("adminNotificationProducerExecutor")
    public void resumeBroadcast(Long adminNotificationId, LocalDateTime cutoff) {
        MDC.put(MDC_KEY, String.valueOf(adminNotificationId));
        try {
            if (!lifecycleService.claimStaleForResume(adminNotificationId, cutoff)) {
                log.info(">>> [AdminNotification] broadcast 재개 건너뜀 - 선점 실패(이미 처리 중)");
                return;
            }
            log.info(">>> [AdminNotification] broadcast 재개");
            runFanOut(adminNotificationId);
        } catch (Exception e) {
            log.error(">>> [AdminNotification] broadcast 재개 실패 cause={}", e.getMessage(), e);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }


    // 청크 단위로 이벤트 발행
    private void runFanOut(Long adminNotificationId) {
        AdminNotificationBroadcastInfo info = targetService.getBroadcastInfo(adminNotificationId);
        Long lastUserId = info.lastBroadcastUserId(); // 청크 단위 유저 조회 진행 커서
        LocalDateTime now = LocalDateTime.now();

        while (true) {
            // 1. 진행 커서 기준 청크 단위 유저 조회
            List<Long> userIds = targetService.findTargetChunk(info.targetAudience(), info.eventTargetId(), lastUserId, now, TARGET_USER_CHUNK_SIZE);
            if (userIds.isEmpty()) break;
            Long chunkLastUserId = userIds.get(userIds.size() - 1);

            // 2. 청크 단위 발송 로그 선저장
            targetService.persistPendingChunk(
                    adminNotificationId, userIds, LocalDateTime.now().plusMinutes(RELAY_GRACE_MINUTES), chunkLastUserId);

            // 3. 청크 단위 발송 이벤트 발행
            adminNotificationPublisher.publishChunk(
                    AdminNotificationChunkEvent.of(adminNotificationId, info, userIds));

            lastUserId = chunkLastUserId;

            if (userIds.size() < TARGET_USER_CHUNK_SIZE) break;
        }

        // 4. 이벤트 발행 완료 표시
        int targetCount = targetService.countEnrolled(adminNotificationId);
        log.info(">>> [AdminNotification] 모든 chunk 발행 완료 targetCount={}", targetCount);
        lifecycleService.markAllChunkPublished(adminNotificationId, targetCount);
    }
}
