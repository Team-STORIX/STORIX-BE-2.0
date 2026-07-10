package com.storix.domain.domains.notification.repository;

import com.storix.domain.domains.notification.domain.AdminNotificationLog;
import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;
import com.storix.domain.domains.notification.dto.AdminNotificationLogStatusCount;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AdminNotificationLogRepository extends JpaRepository<AdminNotificationLog, Long> {

    // 청크 내 기존 로그 조회
    List<AdminNotificationLog> findByAdminNotificationIdAndUserIdIn(Long adminNotificationId, Collection<Long> userIds);

    // 청크 발송 선점
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
        SELECT l FROM AdminNotificationLog l
        WHERE l.adminNotificationId = :id AND l.userId IN :userIds
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    List<AdminNotificationLog> lockClaimablePending(@Param("id") Long id, @Param("userIds") Collection<Long> userIds);

    // 지연 재시도 대상 조회
    @Query("""
        SELECT l FROM AdminNotificationLog l
        WHERE l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
          AND l.nextRetryAt <= :now
        ORDER BY l.nextRetryAt ASC
    """)
    List<AdminNotificationLog> findDueRetryable(@Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    // 발송 중으로 멈춘 로그 복구
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING,
            l.nextRetryAt = :now
        WHERE l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SENDING
          AND l.updatedAt < :cutoff
    """)
    int resetStaleSending(@Param("cutoff") LocalDateTime cutoff, @Param("now") LocalDateTime now);

    // 전체 로그 수
    long countByAdminNotificationId(Long adminNotificationId);

    // 미처리 로그 존재 여부
    boolean existsByAdminNotificationIdAndStatusIn(Long adminNotificationId, Collection<AdminNotificationLogStatus> statuses);

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
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SENDING
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
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SENDING
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
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SENDING
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

    // 강제 종료 시 미처리 로그 실패 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.FAILED,
            l.nextRetryAt = null
        WHERE l.adminNotificationId = :id
          AND l.status IN (com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING,
                           com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.SENDING)
    """)
    int failIncompleteLogs(@Param("id") Long id);

    // 야간 마케팅 발송 연기 - PENDING 로그의 재시도 시각만 미룸 (status/attempts 불변)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotificationLog l
        SET l.nextRetryAt = :deferUntil
        WHERE l.adminNotificationId = :id
          AND l.userId IN :userIds
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
    """)
    int deferPending(@Param("id") Long id, @Param("userIds") List<Long> userIds, @Param("deferUntil") LocalDateTime deferUntil);

    // 미래 재시도가 예약된 PENDING 로그 존재 여부 (강제 종료 보류 판단)
    @Query("""
        SELECT (COUNT(l) > 0) FROM AdminNotificationLog l
        WHERE l.adminNotificationId = :id
          AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
          AND l.nextRetryAt > :now
    """)
    boolean existsScheduledRetry(@Param("id") Long id, @Param("now") LocalDateTime now);
}
