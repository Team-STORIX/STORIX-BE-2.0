package com.storix.domain.domains.notification.repository;

import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;
import com.storix.domain.domains.notification.dto.AdminNotificationLogStatusCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AdminNotificationLogRepository extends JpaRepository<AdminNotificationLog, Long> {

    // 청크 내 기존 로그 조회
    List<AdminNotificationLog> findByAdminNotificationIdAndUserIdIn(Long adminNotificationId, Collection<Long> userIds);

    // 지연 재시도 대상 원자적 선점
    @Query(value = """
        SELECT * FROM admin_notification_log
        WHERE status = 'PENDING' AND next_retry_at <= :now
        ORDER BY next_retry_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<AdminNotificationLog> findRetryableForUpdate(@Param("now") LocalDateTime now, @Param("limit") int limit);

    // 선점한 재시도 로그 재선점 방지
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.nextRetryAt = :lease
        WHERE l.id IN :ids
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    void leaseRetry(@Param("ids") List<Long> ids, @Param("lease") LocalDateTime lease);

    // 전체 로그 수
    long countByAdminNotificationId(Long adminNotificationId);

    // 미처리 로그 존재 여부
    boolean existsByAdminNotificationIdAndStatus(Long adminNotificationId, AdminNotificationLogStatus status);

    // 상태별 로그 수 - 종료 집계용
    @Query("""
        SELECT new com.storix.domain.domains.notification.dto.AdminNotificationLogStatusCount(l.status, COUNT(l))
        FROM AdminNotificationLog l
        WHERE l.adminNotificationId = :id
        GROUP BY l.status
    """)
    List<AdminNotificationLogStatusCount> countGroupByStatus(@Param("id") Long id);

    // 발송 성공 반영
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SENT,
            l.sentAt = :now,
            l.attempts = l.attempts + 1,
            l.nextRetryAt = null
        WHERE l.adminNotificationId = :id
          AND l.userId IN :userIds
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    void markSent(@Param("id") Long id, @Param("userIds") List<Long> userIds, @Param("now") LocalDateTime now);

    // 발송 대상 외 반영
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SKIPPED,
            l.nextRetryAt = null
        WHERE l.adminNotificationId = :id
          AND l.userId IN :userIds
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    void markSkipped(@Param("id") Long id, @Param("userIds") List<Long> userIds);

    // 영구·설정 오류 반영
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.FAILED,
            l.attempts = l.attempts + 1,
            l.nextRetryAt = null
        WHERE l.adminNotificationId = :id
          AND l.userId IN :userIds
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    void markPermanentFailed(@Param("id") Long id, @Param("userIds") List<Long> userIds);

    // 수동 재발송 시 로그를 재시도 스캔 대상으로 전환
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING,
            l.attempts = 0,
            l.nextRetryAt = :now
        WHERE l.adminNotificationId = :id
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.FAILED
    """)
    int reviveFailedLogs(@Param("id") Long id, @Param("now") LocalDateTime now);

    // 강제 종료 시 로그 실패 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.FAILED,
            l.nextRetryAt = null
        WHERE l.adminNotificationId = :id
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    int failPendingLogs(@Param("id") Long id);
}
