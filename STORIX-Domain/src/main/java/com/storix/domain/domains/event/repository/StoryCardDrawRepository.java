package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.StoryCardDraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface StoryCardDrawRepository extends JpaRepository<StoryCardDraw, Long> {

    Optional<StoryCardDraw> findByAppEventIdAndUserIdAndDrawnOn(Long appEventId, Long userId, LocalDate drawnOn);

    boolean existsByAppEventIdAndUserIdAndDrawnOn(Long appEventId, Long userId, LocalDate drawnOn);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO event_story_card_draws
                (app_event_id, user_id, drawn_on, genre, message, immersion,
                 lucky_work_title, lucky_work_type, lucky_work_platform, lucky_work_landing_url,
                 drawn_at, created_at, updated_at)
            VALUES (:appEventId, :userId, :drawnOn, :genre, :message, :immersion,
                    :luckyWorkTitle, :luckyWorkType, :luckyWorkPlatform, :luckyWorkLandingUrl,
                    :drawnAt, NOW(), NOW())
            ON DUPLICATE KEY UPDATE story_card_draw_id = story_card_draw_id
            """, nativeQuery = true)
    int insertIfAbsent(@Param("appEventId") Long appEventId,
                       @Param("userId") Long userId,
                       @Param("drawnOn") LocalDate drawnOn,
                       @Param("genre") String genreDbValue,
                       @Param("message") String message,
                       @Param("immersion") String immersion,
                       @Param("luckyWorkTitle") String luckyWorkTitle,
                       @Param("luckyWorkType") String luckyWorkType,
                       @Param("luckyWorkPlatform") String luckyWorkPlatform,
                       @Param("luckyWorkLandingUrl") String luckyWorkLandingUrl,
                       @Param("drawnAt") LocalDateTime drawnAt);

    // 지난 카드 정리용. 매일 06:00 배치가 당일 이전 뽑기 결과를 전부 지운다
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM StoryCardDraw d WHERE d.drawnOn < :cutoff")
    int deleteAllDrawnBefore(@Param("cutoff") LocalDate cutoff);
}
