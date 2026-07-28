package com.storix.domain.domains.event.repository;

import com.storix.domain.domains.event.domain.StoryCardMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoryCardMessageRepository extends JpaRepository<StoryCardMessage, Long> {

    // genre는 GenreConverter로 한글 dbValue가 저장돼 있어 네이티브 쿼리에는 dbValue를 넘긴다
    @Query(value = """
            SELECT * FROM event_story_card_messages
            WHERE genre = :genre AND is_active = 1
            ORDER BY RAND() LIMIT 1
            """, nativeQuery = true)
    Optional<StoryCardMessage> findRandomByGenre(@Param("genre") String genreDbValue);
}
