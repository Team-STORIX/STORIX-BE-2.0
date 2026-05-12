package com.storix.domain.domains.topicroom.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

import static com.storix.domain.domains.topicroom.domain.QTopicRoom.topicRoom;
import static com.storix.domain.domains.works.domain.QWorks.works;

@Repository
@RequiredArgsConstructor
public class TopicRoomRankingRepositoryImpl implements TopicRoomRankingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory queryFactory;

    // 여러 개의 방 인기도 점수와 증가율을 한 번에 업데이트
    @Override
    @Transactional
    public void bulkUpdatePopularity(List<TopicRoom> rooms) {

        String sql = "UPDATE topic_room SET popularity_score = ?, popularity_growth_rate = ? WHERE topic_room_id = ?";

        jdbcTemplate.batchUpdate(sql,
                rooms,
                1000,
                (PreparedStatement ps, TopicRoom room) -> {
                    ps.setDouble(1, room.getPopularityScore());
                    ps.setDouble(2, room.getPopularityGrowthRate());
                    ps.setLong(3, room.getId());
                });
    }

    // 여러 개의 방 이전 참여자 수를 한 번에 업데이트
    @Override
    @Transactional
    public void bulkUpdatePreviousActiveUserNumbers(List<TopicRoom> rooms) {

        String sql = "UPDATE topic_room SET previous_active_user_number = ? WHERE topic_room_id = ?";

        jdbcTemplate.batchUpdate(sql,
                rooms,
                1000,
                (PreparedStatement ps, TopicRoom room) -> {
                    ps.setInt(1, room.getPreviousActiveUserNumber());
                    ps.setLong(2, room.getId());
                });
    }

    // 공통 필터: 참여자 2명 이상 + 최근 메시지 72시간 이내
    private BooleanBuilder commonFilter() {
        LocalDateTime now = LocalDateTime.now();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(topicRoom.activeUserNumber.goe(2));
        builder.and(topicRoom.lastChatTime.after(now.minusHours(72)));
        return builder;
    }

    // 충성 유저 탐색 필터 - 슬롯 1개
    @Override
    public List<TopicRoomResponseDto> findLoyaltyRooms() {

        BooleanBuilder condition = commonFilter();
        condition.and(topicRoom.activeUserNumber.goe(5));

        // 절대 증가 수: 현재 참여자 수 - 24시간 전 참여자 수
        NumberExpression<Integer> absoluteGrowth =
                topicRoom.activeUserNumber.subtract(topicRoom.previousActiveUserNumber);

        return queryFactory
                .select(Projections.constructor(TopicRoomResponseDto.class,
                        topicRoom.id,
                        topicRoom.topicRoomName,
                        works.worksType,
                        works.worksName,
                        works.thumbnailUrl,
                        topicRoom.activeUserNumber,
                        topicRoom.lastChatTime,
                        Expressions.constant(false)
                ))
                .from(topicRoom)
                .join(works).on(topicRoom.worksId.eq(works.id))
                .where(condition)
                .orderBy(
                        topicRoom.popularityGrowthRate.desc(),   // 증가율 내림차순
                        absoluteGrowth.desc(),                   // 동점 시 절대 증가 수 우선
                        topicRoom.id.desc()                      // 동점 시 최신 방 우선
                )
                .limit(1)
                .fetch();
    }

    // 신규 유저 락인 필터 - 슬롯 2개 ~ 3개
    @Override
    public List<TopicRoomResponseDto> findNewUserRooms(List<Long> excludeIds, int limit) {

        LocalDateTime now = LocalDateTime.now();

        BooleanBuilder condition = commonFilter();
        // 최근 24시간 내 메시지 1개 이상 존재
        condition.and(topicRoom.lastChatTime.after(now.minusHours(24)));

        if (excludeIds != null && !excludeIds.isEmpty()) {
            condition.and(topicRoom.id.notIn(excludeIds));
        }

        return queryFactory
                .select(Projections.constructor(TopicRoomResponseDto.class,
                        topicRoom.id,
                        topicRoom.topicRoomName,
                        works.worksType,
                        works.worksName,
                        works.thumbnailUrl,
                        topicRoom.activeUserNumber,
                        topicRoom.lastChatTime,
                        Expressions.constant(false)
                ))
                .from(topicRoom)
                .join(works).on(topicRoom.worksId.eq(works.id))
                .where(condition)
                .orderBy(
                        topicRoom.activeUserNumber.desc(),  // 참여자 수 내림차순
                        topicRoom.lastChatTime.desc()       // 동점 시 최근 메시지 전송 시간 우선
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public List<TopicRoomResponseDto> findTop5PopularRooms() {
        return queryFactory
                .select(Projections.constructor(TopicRoomResponseDto.class,
                        topicRoom.id,
                        topicRoom.topicRoomName,
                        works.worksType,
                        works.worksName,
                        works.thumbnailUrl,
                        topicRoom.activeUserNumber,
                        topicRoom.lastChatTime,
                        Expressions.constant(false)
                ))
                .from(topicRoom)
                .join(works).on(topicRoom.worksId.eq(works.id))
                .orderBy(
                        topicRoom.popularityScore.desc(),
                        topicRoom.lastChatTime.desc()
                )
                .limit(5)
                .fetch();
    }
}
