package com.storix.domain.domains.notification.repository;

import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.domain.AdminNotificationStatus;
import com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    Page<AdminNotification> findAllByOrderByIdDesc(Pageable pageable);

    // 발송용 어드민 알림 조회 (lastBroadcastUserId = 재개 시작 커서)
    @Query("""
        SELECT new com.storix.domain.domains.notification.dto.AdminNotificationBroadcastInfo(e.title, e.content, e.notificationType, e.targetAudience, e.targetType, e.eventTargetId, e.targetLink, e.lastBroadcastUserId)
        FROM AdminNotification e
        WHERE e.id = :id
    """)
    Optional<AdminNotificationBroadcastInfo> findBroadcastInfo(@Param("id") Long id);

    // 어드민 알림 수정/취소 시 발송 상태 경합 방지용 비관적 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM AdminNotification e WHERE e.id = :id")
    Optional<AdminNotification> findByIdForUpdate(@Param("id") Long id);

    // 청크 발송 로그 선저장 + 진행 커서 전진
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.lastBroadcastUserId = :cursor,
            e.updatedAt = :now
        WHERE e.id = :id
    """)
    void advanceBroadcastCursor(@Param("id") Long id,
                                @Param("cursor") Long cursor,
                                @Param("now") LocalDateTime now);

    // 발송 시작 원자적 상태 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING,
            e.isAllChunkPublished = false,
            e.updatedAt = :now
        WHERE e.id = :id
          AND e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SCHEDULED
    """)
    int startSending(@Param("id") Long id, @Param("now") LocalDateTime now);

    // 수동 재발송 상태 변경
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING,
            e.isAllChunkPublished = true,
            e.updatedAt = :now
        WHERE e.id = :id
          AND e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.FAILED
    """)
    int startRebroadcast(@Param("id") Long id, @Param("now") LocalDateTime now);

    // 재시도 진행 표시
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.updatedAt = :now
        WHERE e.id = :id
          AND e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING
          AND e.isAllChunkPublished = true
    """)
    void touchProgress(@Param("id") Long id, @Param("now") LocalDateTime now);

    // 재개 원자적 선점
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.updatedAt = :now
        WHERE e.id = :id
          AND e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING
          AND e.isAllChunkPublished = false
          AND e.updatedAt < :cutoff
    """)
    int claimStaleForResume(@Param("id") Long id, @Param("cutoff") LocalDateTime cutoff, @Param("now") LocalDateTime now);

    // 예약 상태이면서 예약 시각이 지난 발송 대상
    @Query("""
        SELECT c
        FROM AdminNotification c
        WHERE c.status = :status
          AND c.scheduledAt <= :now
        ORDER BY c.scheduledAt ASC, c.id ASC
    """)
    List<AdminNotification> findDueNotifications(
            @Param("status") AdminNotificationStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // 발송 결과 원자적 누적 + updatedAt 갱신
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.sentCount = e.sentCount + :sent,
            e.failedCount = e.failedCount + :failed,
            e.skippedCount = e.skippedCount + :skipped,
            e.updatedAt = :now
        WHERE e.id = :id
    """)
    void addCounts(@Param("id") Long id,
                   @Param("sent") int sent,
                   @Param("failed") int failed,
                   @Param("skipped") int skipped,
                   @Param("now") LocalDateTime now);

    // 모든 청크 발행 완료 표시 + 전체 대상 수 확정
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.targetCount = :total,
            e.isAllChunkPublished = true,
            e.updatedAt = :now
        WHERE e.id = :id
    """)
    void markAllChunkPublished(@Param("id") Long id,
                               @Param("total") int total,
                               @Param("now") LocalDateTime now);

    // SENDING 일 때만 종료 + 최종 카운트 확정
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE AdminNotification e
        SET e.status = :status,
            e.sentCount = :sent,
            e.failedCount = :failed,
            e.skippedCount = :skipped,
            e.updatedAt = :now
        WHERE e.id = :id
          AND e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING
    """)
    int finalizeIfSending(@Param("id") Long id,
                          @Param("status") AdminNotificationStatus status,
                          @Param("sent") int sent,
                          @Param("failed") int failed,
                          @Param("skipped") int skipped,
                          @Param("now") LocalDateTime now);

    // 발행 완료인데 아직 SENDING 인 발송 (종료 대상)
    @Query("""
        SELECT e.id FROM AdminNotification e
        WHERE e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING
          AND e.isAllChunkPublished = true
          AND NOT EXISTS (
              SELECT 1 FROM AdminNotificationLog l
              WHERE l.adminNotificationId = e.id
                AND l.status = com.storix.domain.domains.notification.domain.AdminNotificationLogStatus.PENDING
          )
        ORDER BY e.id ASC
    """)
    List<Long> findCompletableSendingIds(Pageable pageable);

    // 발행 도중 멈춘 발송 (커서부터 재개 대상)
    @Query("""
        SELECT e.id FROM AdminNotification e
        WHERE e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING
          AND e.isAllChunkPublished = false
          AND e.updatedAt < :cutoff
        ORDER BY e.updatedAt ASC
    """)
    List<Long> findStaleIncompleteSendingIds(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    // 발행은 끝났는데 멈춘 발송 (강제 종료 대상)
    @Query("""
        SELECT e.id FROM AdminNotification e
        WHERE e.status = com.storix.domain.domains.notification.domain.AdminNotificationStatus.SENDING
          AND e.isAllChunkPublished = true
          AND e.updatedAt < :cutoff
        ORDER BY e.updatedAt ASC
    """)
    List<Long> findStaleCompletedSendingIds(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);
}
