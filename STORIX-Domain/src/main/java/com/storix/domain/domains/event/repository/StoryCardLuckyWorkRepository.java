package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.StoryCardLuckyWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoryCardLuckyWorkRepository extends JpaRepository<StoryCardLuckyWork, Long> {

    @Query(value = """
            SELECT * FROM event_story_card_lucky_works
            WHERE genre = :genre AND is_active = 1
            ORDER BY RAND() LIMIT 1
            """, nativeQuery = true)
    Optional<StoryCardLuckyWork> findRandomByGenre(@Param("genre") String genreDbValue);
}
