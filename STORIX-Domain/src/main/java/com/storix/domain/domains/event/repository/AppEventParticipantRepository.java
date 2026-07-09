package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AppEventParticipant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppEventParticipantRepository extends JpaRepository<AppEventParticipant, Long> {

    // EVENT_WINNERS 알림 대상 - 당첨자 userId 커서 페이지네이션
    @Query("""
        SELECT p.userId
        FROM AppEventParticipant p
        WHERE p.appEventId = :appEventId
          AND p.winner = true
          AND (:lastUserId IS NULL OR p.userId > :lastUserId)
        ORDER BY p.userId ASC
    """)
    List<Long> findWinnerUserIds(
            @Param("appEventId") Long appEventId,
            @Param("lastUserId") Long lastUserId,
            Pageable pageable
    );

    // 당첨자 upsert: 참여행이 있으면 당첨 승격, 없으면 신규 생성.
    // 동시 참여로 인한 유니크 충돌을 DB에서 흡수한다.
    @Modifying
    @Query(value = """
        INSERT INTO app_event_participants (app_event_id, user_id, is_winner, created_at, updated_at)
        VALUES (:appEventId, :userId, true, NOW(), NOW())
        ON DUPLICATE KEY UPDATE is_winner = true, updated_at = NOW()
    """, nativeQuery = true)
    void upsertWinner(@Param("appEventId") Long appEventId, @Param("userId") Long userId);
}
