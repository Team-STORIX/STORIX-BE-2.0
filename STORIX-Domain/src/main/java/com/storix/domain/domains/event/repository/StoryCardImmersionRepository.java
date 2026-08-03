package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.StoryCardImmersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StoryCardImmersionRepository extends JpaRepository<StoryCardImmersion, Long> {

    // 몰입력은 장르와 무관하게 전체 풀에서 뽑는다
    @Query(value = """
            SELECT * FROM event_story_card_immersions
            WHERE is_active = 1
            ORDER BY RAND() LIMIT 1
            """, nativeQuery = true)
    Optional<StoryCardImmersion> findRandom();
}
