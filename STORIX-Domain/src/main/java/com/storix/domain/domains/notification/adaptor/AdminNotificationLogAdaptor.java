package com.storix.domain.domains.notification.adaptor;

import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;
import com.storix.domain.domains.notification.repository.AdminNotificationLogRepository;
import lombok.RequiredArgsConstructor;
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

    public List<AdminNotificationLog> findRetryableForUpdate(LocalDateTime now, int limit) {
        return adminNotificationLogRepository.findRetryableForUpdate(now, limit);
    }

    public long countTotal(Long adminNotificationId) {
        return adminNotificationLogRepository.countByAdminNotificationId(adminNotificationId);
    }

    public boolean existsByStatus(Long adminNotificationId, AdminNotificationLogStatus status) {
        return adminNotificationLogRepository.existsByAdminNotificationIdAndStatus(adminNotificationId, status);
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

    public void leaseRetry(List<Long> ids, LocalDateTime lease) {
        adminNotificationLogRepository.leaseRetry(ids, lease);
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

    public int failPendingLogs(Long adminNotificationId) {
        return adminNotificationLogRepository.failPendingLogs(adminNotificationId);
    }
}
