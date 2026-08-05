package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.AppEventWinner;
import com.storix.domain.domains.event.dto.EventWinner;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppEventWinnerRepository extends JpaRepository<AppEventWinner, Long> {

    // EVENT_WINNERS 알림 대상 - 당첨자 userId 커서 페이지네이션
    @Query("""
        SELECT w.userId
        FROM AppEventWinner w
        WHERE w.appEventId = :appEventId
          AND (:lastUserId IS NULL OR w.userId > :lastUserId)
        ORDER BY w.userId ASC
    """)
    List<Long> findWinnerUserIds(
            @Param("appEventId") Long appEventId,
            @Param("lastUserId") Long lastUserId,
            Pageable pageable
    );

    // 확정 여부 판정용
    boolean existsByAppEventId(Long appEventId);

    // 확정된 당첨자를 뽑힌 순서대로
    @Query("""
        SELECT new com.storix.domain.domains.event.dto.EventWinner(w.userId, w.drawOrder)
        FROM AppEventWinner w
        WHERE w.appEventId = :appEventId
        ORDER BY w.drawOrder ASC
    """)
    List<EventWinner> findWinners(@Param("appEventId") Long appEventId);

    // 당첨자 저장
    @Modifying
    @Query(value = """
        INSERT INTO app_event_winners (app_event_id, user_id, draw_order, created_at, updated_at)
        VALUES (:appEventId, :userId, :drawOrder, NOW(), NOW())
        ON DUPLICATE KEY UPDATE app_event_winner_id = app_event_winner_id
    """, nativeQuery = true)
    void insertWinnerIfAbsent(@Param("appEventId") Long appEventId,
                              @Param("userId") Long userId,
                              @Param("drawOrder") int drawOrder);
}
