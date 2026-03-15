package com.storix.storix_api.domains.topicroom.repository;

import com.storix.storix_api.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TopicRoomRankingRepositoryImpl implements TopicRoomRankingRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void bulkUpdatePopularityScores(List<TopicRoom> rooms) {

        String sql = "UPDATE topic_room SET popularity_score = ? WHERE topic_room_id = ?";

        // 배치 크기 설정
        jdbcTemplate.batchUpdate(sql,
                rooms,
                1000,
                (PreparedStatement ps, TopicRoom room) -> {
                    ps.setDouble(1, room.getPopularityScore());
                    ps.setLong(2, room.getId());
                });
    }
}
