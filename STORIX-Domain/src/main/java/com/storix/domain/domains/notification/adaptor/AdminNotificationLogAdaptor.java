package com.storix.domain.domains.notification.adaptor;

import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;
import com.storix.domain.domains.notification.repository.AdminNotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AdminNotificationLogAdaptor {

    private final AdminNotificationLogRepository adminNotificationLogRepository;

    /* ===== 읽기 작업 ===== */

    public List<AdminNotificationLog> findByChunk(Long adminNotificationId, Collection<Long> userIds) {
        return adminNotificationLogRepository.findByAdminNotificationIdAndUserIdIn(adminNotificationId, userIds);
    }

    // 청크 발송 선점
    public List<AdminNotificationLog> lockClaimablePending(Long adminNotificationId, Collection<Long> userIds) {
        // 발송 대기 로그를 잠가서 반환 (SKIP LOCKED)
        return adminNotificationLogRepository.lockClaimablePending(adminNotificationId, userIds);
    }

    // 지연 재시도 대상 조회
    public List<AdminNotificationLog> findDueRetryable(LocalDateTime now, int limit) {
        return adminNotificationLogRepository.findDueRetryable(now, PageRequest.of(0, limit));
    }

    public long countTotal(Long adminNotificationId) {
        return adminNotificationLogRepository.countByAdminNotificationId(adminNotificationId);
    }

    // 미처리 로그 존재 여부
    public boolean existsIncomplete(Long adminNotificationId) {
        return adminNotificationLogRepository.existsByAdminNotificationIdAndStatusIn(
                adminNotificationId,
                List.of(AdminNotificationLogStatus.PENDING, AdminNotificationLogStatus.SENDING));
    }

    public Map<AdminNotificationLogStatus, Integer> countGroupByStatus(Long adminNotificationId) {
        Map<AdminNotificationLogStatus, Integer> counts = new EnumMap<>(AdminNotificationLogStatus.class);
        adminNotificationLogRepository.countGroupByStatus(adminNotificationId)
                .forEach(row -> counts.put(row.status(), row.count().intValue()));
        return counts;
    }


    /* ===== 쓰기 작업 ===== */

    public void saveAll(List<AdminNotificationLog> logs) {
        adminNotificationLogRepository.saveAll(logs);
    }

    // 발송 중으로 멈춘 로그를 발송 대기 상태로 복구
    public int resetStaleSending(LocalDateTime cutoff, LocalDateTime now) {
        return adminNotificationLogRepository.resetStaleSending(cutoff, now);
    }

    public void markSent(Long adminNotificationId, List<Long> userIds, LocalDateTime now) {
        adminNotificationLogRepository.markSent(adminNotificationId, userIds, now);
    }

    public void markSkipped(Long adminNotificationId, List<Long> userIds) {
        adminNotificationLogRepository.markSkipped(adminNotificationId, userIds);
    }

    public void markPermanentFailed(Long adminNotificationId, List<Long> userIds) {
        adminNotificationLogRepository.markPermanentFailed(adminNotificationId, userIds);
    }

    public int reviveFailedLogs(Long adminNotificationId, LocalDateTime now) {
        return adminNotificationLogRepository.reviveFailedLogs(adminNotificationId, now);
    }

    public int failIncompleteLogs(Long adminNotificationId) {
        return adminNotificationLogRepository.failIncompleteLogs(adminNotificationId);
    }
}
