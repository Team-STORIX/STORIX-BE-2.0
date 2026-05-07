package com.storix.domain.domains.genrescore.repository;

import com.storix.domain.domains.genrescore.domain.UserGenreScoreLog;
import com.storix.domain.domains.genrescore.dto.UnprocessedLogRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserGenreScoreLogRepository extends JpaRepository<UserGenreScoreLog, Long> {

    // 미처리 로그를 id 오름차순, 청크 단위로 조회
    @Query("""
            SELECT new com.storix.domain.domains.genrescore.dto.UnprocessedLogRow(l.id, l.userId, l.genre, l.weight)
            FROM UserGenreScoreLog l
            WHERE l.processedAt IS NULL
            ORDER BY l.id ASC
    """)
    List<UnprocessedLogRow> findUnprocessedChunk(Pageable pageable);

    // 미처리 로그 -> 처리 로그 (벌크 업데이트)
    @Modifying
    @Query("""
            UPDATE UserGenreScoreLog l
            SET l.processedAt = :now
            WHERE l.processedAt IS NULL AND l.id <= :maxId
    """)
    int markProcessedUntil(@Param("maxId") Long maxId, @Param("now") LocalDateTime now);


    @Modifying
    @Query("DELETE FROM UserGenreScoreLog l WHERE l.processedAt IS NOT NULL AND l.processedAt < :threshold")
    int deleteProcessedBefore(@Param("threshold") LocalDateTime threshold);
}
