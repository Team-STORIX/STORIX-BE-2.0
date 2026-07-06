package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.AdminNotificationAdaptor;
import com.storix.domain.domains.notification.adaptor.AdminNotificationLogAdaptor;
import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;
import com.storix.domain.domains.notification.domain.AdminNotificationStatus;
import com.storix.domain.domains.notification.dto.AdminNotificationStartResult;
import com.storix.domain.domains.notification.exception.AdminNotificationNotRebroadcastableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNotificationLifecycleService {

    private static final int DUE_NOTIFICATION_SIZE = 20;
    private static final int RECONCILE_BATCH_SIZE = 20;

    private final AdminNotificationAdaptor adminNotificationAdaptor;
    private final AdminNotificationLogAdaptor adminNotificationLogAdaptor;

    /* ===== 상태 변경 ===== */

    // 예약중인 운영자 알림 -> 발송 가능 상태로 변경
    @Transactional
    public AdminNotificationStartResult startSending(Long adminNotificationId) {
        // [AdminNotification] SCHEDULED -> SENDING
        int updated = adminNotificationAdaptor.startSending(adminNotificationId, LocalDateTime.now());
        if (updated > 0) {
            return new AdminNotificationStartResult(true, AdminNotificationStatus.SENDING);
        }

        // 동시 접근 시 후건(updated = 0)에서 발송 상태 재조회
        AdminNotificationStatus current = adminNotificationAdaptor.findById(adminNotificationId).getStatus();
        return new AdminNotificationStartResult(false, current);
    }

    // 실패된 운영자 알림 -> 발송 가능 상태로 변경
    @Transactional
    public void prepareRebroadcast(Long adminNotificationId) {
        // 1. [AdminNotification] FAILED -> SENDING
        int transitioned = adminNotificationAdaptor.startRebroadcast(adminNotificationId, LocalDateTime.now());
        if (transitioned == 0) {
            throw AdminNotificationNotRebroadcastableException.EXCEPTION;
        }

        // 2. [AdminNotificationLog] FAILED -> PENDING, nextRetryAt = now 로 되살림 => 재시도 스케줄러가 발송
        int revived = adminNotificationLogAdaptor.reviveFailedLogs(adminNotificationId, LocalDateTime.now());
        log.info(">>> [AdminNotification] 재발송 준비 - 실패 로그 revive count={} (재시도 스케줄러가 발송)", revived);
    }

    // 모든 청크 이벤트 발행 완료 표시 후 완료 시도
    @Transactional
    public void markAllChunkPublished(Long adminNotificationId, int targetCount) {
        adminNotificationAdaptor.markAllChunkPublished(adminNotificationId, targetCount, LocalDateTime.now());
        tryFinalize(adminNotificationId);
    }


    /* ===== 완료 판정 ===== */

    // 완료 시도 - 발행 완료 && 미처리 로그가 없으면 종료
    @Transactional
    public void tryFinalize(Long adminNotificationId) {
        AdminNotification adminNotification = adminNotificationAdaptor.findById(adminNotificationId);

        // 1. 발송 중 & 발행 완료 상태에서만
        if (adminNotification.getStatus() != AdminNotificationStatus.SENDING) return;
        if (!adminNotification.isAllChunkPublished()) return;

        // 2. 미처리 로그가 남아있으면 아직 미완료
        if (adminNotificationLogAdaptor.existsIncomplete(adminNotificationId)) return;

        // 3. AdminNotificationLog 기준으로 최종 카운트 집계
        Map<AdminNotificationLogStatus, Integer> counts = adminNotificationLogAdaptor.countGroupByStatus(adminNotificationId);
        int sent = counts.getOrDefault(AdminNotificationLogStatus.SENT, 0);
        int failed = counts.getOrDefault(AdminNotificationLogStatus.FAILED, 0);
        int skipped = counts.getOrDefault(AdminNotificationLogStatus.SKIPPED, 0);
        AdminNotificationStatus finalStatus = failed == 0
                ? AdminNotificationStatus.SENT
                : AdminNotificationStatus.FAILED;

        // 4. SENDING 일 때만 원자적 종료
        int updated = adminNotificationAdaptor.finalizeIfSending(
                adminNotificationId, finalStatus, sent, failed, skipped, LocalDateTime.now());
        if (updated > 0) {
            log.info(">>> [AdminNotification] 발송 종료 adminNotificationId={} status={} sent={} failed={} skipped={}",
                    adminNotificationId, finalStatus, sent, failed, skipped);
        }
    }

    // [AdminNotification] updatedAt 갱신
    @Transactional
    public void touchProgress(Long adminNotificationId) {
        adminNotificationAdaptor.touchProgress(adminNotificationId, LocalDateTime.now());
    }

    // 정체된 발송 강제 종료
    @Transactional
    public void forceFinalize(Long adminNotificationId) {
        AdminNotification adminNotification = adminNotificationAdaptor.findById(adminNotificationId);
        if (adminNotification.getStatus() != AdminNotificationStatus.SENDING) return;

        // 1. 남은 [AdminNotificationLog] 미처리 -> FAILED
        int closed = adminNotificationLogAdaptor.failIncompleteLogs(adminNotificationId);

        // 2. 로그 기준으로 최종 카운트 집계
        Map<AdminNotificationLogStatus, Integer> counts = adminNotificationLogAdaptor.countGroupByStatus(adminNotificationId);
        int sent = counts.getOrDefault(AdminNotificationLogStatus.SENT, 0);
        int failed = counts.getOrDefault(AdminNotificationLogStatus.FAILED, 0);
        int skipped = counts.getOrDefault(AdminNotificationLogStatus.SKIPPED, 0);
        int updated = adminNotificationAdaptor.finalizeIfSending(
                adminNotificationId, AdminNotificationStatus.FAILED, sent, failed, skipped, LocalDateTime.now());
        if (updated > 0) {
            log.warn(">>> [AdminNotification] 강제 종료 (stale) adminNotificationId={} status={} sent={} failed={} skipped={} closedPending={}",
                    adminNotificationId, AdminNotificationStatus.FAILED, sent, failed, skipped, closed);
        }
    }

    /* ===== Scheduler / Retryer ===== */

    // 예약 시각이 된 발송 대상
    @Transactional(readOnly = true)
    public List<AdminNotification> findDueNotifications(LocalDateTime now) {
        return adminNotificationAdaptor.findDue(now, PageRequest.of(0, DUE_NOTIFICATION_SIZE));
    }

    // 발행 완료(미처리 로그 0)인데 아직 SENDING 인 발송 (종료 대상)
    @Transactional(readOnly = true)
    public List<Long> findCompletableSendingIds() {
        return adminNotificationAdaptor.findCompletableSendingIds(PageRequest.of(0, RECONCILE_BATCH_SIZE));
    }

    // 발행 도중 멈춘 발송 (커서부터 재개 대상)
    @Transactional(readOnly = true)
    public List<Long> findStaleIncompleteSendingIds(LocalDateTime cutoff) {
        return adminNotificationAdaptor.findStaleIncompleteSendingIds(cutoff, PageRequest.of(0, RECONCILE_BATCH_SIZE));
    }

    // 발행은 끝났는데 멈춘 발송 (강제 종료 대상)
    @Transactional(readOnly = true)
    public List<Long> findStaleCompletedSendingIds(LocalDateTime cutoff) {
        return adminNotificationAdaptor.findStaleCompletedSendingIds(cutoff, PageRequest.of(0, RECONCILE_BATCH_SIZE));
    }

    // 재개 원자적 선점
    @Transactional
    public boolean claimStaleForResume(Long adminNotificationId, LocalDateTime cutoff) {
        return adminNotificationAdaptor.claimStaleForResume(adminNotificationId, cutoff, LocalDateTime.now()) > 0;
    }

    // 지연 재시도 대상 조회
    @Transactional(readOnly = true)
    public List<AdminNotificationLog> findDueRetryable(LocalDateTime now, int limit) {
        return adminNotificationLogAdaptor.findDueRetryable(now, limit);
    }

    // 전송 중으로 멈춘 로그 회수
    @Transactional
    public int resetStaleSendingLogs(LocalDateTime cutoff) {
        return adminNotificationLogAdaptor.resetStaleSending(cutoff, LocalDateTime.now());
    }
}
