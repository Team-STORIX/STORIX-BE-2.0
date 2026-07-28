package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.StoryCardDraw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StoryCardDrawRepository extends JpaRepository<StoryCardDraw, Long> {

    Optional<StoryCardDraw> findByAppEventIdAndUserIdAndDrawnOn(Long appEventId, Long userId, LocalDate drawnOn);

    boolean existsByAppEventIdAndUserIdAndDrawnOn(Long appEventId, Long userId, LocalDate drawnOn);

    // 지난 카드 정리용. 매일 06:00 배치가 당일 이전 뽑기 결과를 전부 지운다
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM StoryCardDraw d WHERE d.drawnOn < :cutoff")
    int deleteAllDrawnBefore(@Param("cutoff") LocalDate cutoff);
}
