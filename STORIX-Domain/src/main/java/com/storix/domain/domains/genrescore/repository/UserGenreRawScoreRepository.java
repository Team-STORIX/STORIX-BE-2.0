package com.storix.domain.domains.genrescore.repository;

import com.storix.domain.domains.genrescore.domain.UserGenreRawScore;
import com.storix.domain.domains.genrescore.domain.UserGenreRawScoreId;
import com.storix.domain.domains.works.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserGenreRawScoreRepository extends JpaRepository<UserGenreRawScore, UserGenreRawScoreId> {

    List<UserGenreRawScore> findAllByIdUserId(Long userId);

    @Modifying
    @Query(value = """
            INSERT INTO user_genre_raw_score (user_id, genre, raw_score, created_at, updated_at)
            VALUES (:userId, :genre, :delta, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                raw_score = raw_score + :delta,
                updated_at = NOW()
            """, nativeQuery = true)
    void upsertAdd(@Param("userId") Long userId,
                   @Param("genre") String genre,
                   @Param("delta") long delta);

    default void upsertAdd(Long userId, Genre genre, long delta) {
        upsertAdd(userId, genre.name(), delta);
    }
}
