package com.storix.domain.domains.plus.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ReaderBoardRankingRepositoryImpl implements ReaderBoardRankingRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public int updatePopularityScoresRecentDays(LocalDateTime threshold) {

        // 스케줄러 호출 기준 7일 이내 생성된 게시글 및 좋아요/댓글 수 변동 있는 게시글만 점수 업데이트
        String sql = """
            UPDATE reader_board
            SET popularity_score = (like_count * 3 + reply_count * 4)
            WHERE created_at >= ?
                AND (popularity_score IS NULL OR popularity_score <> (like_count*3 + reply_count*4))
        """;

        return jdbcTemplate.update(sql, threshold);
    }
}
