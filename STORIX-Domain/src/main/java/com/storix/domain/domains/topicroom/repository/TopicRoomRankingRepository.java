package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.dto.RoomMessageCount;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface TopicRoomRankingRepository {

    // 여러 개의 방 인기도 점수와 증가율을 한 번에 업데이트
    void bulkUpdatePopularity(List<TopicRoom> rooms);

    // 여러 개의 방 이전 참여자 수를 한 번에 업데이트
    void bulkUpdatePreviousActiveUserNumbers(List<TopicRoom> rooms);

    // 활동 점수 대상 방 id
    List<Long> findActivityScoreTargetRoomIds();

    // 방별 since 이후 메시지 수 (메시지 없는 방은 빠짐)
    List<RoomMessageCount> countMessagesSince(List<Long> roomIds, LocalDateTime since);

    // 여러 개의 방 활동 점수를 한 번에 업데이트
    void bulkUpdateActivityScores(List<RoomMessageCount> counts, LocalDateTime freshnessSince);

    // 충성 슬롯: 증가율 기준 상위 1개
    List<TopicRoomResponseDto> findLoyaltySlot();

    // 신규 슬롯: 참여자 수 기준 상위 2개
    List<TopicRoomResponseDto> findNewUserSlots(List<Long> excludeIds, int limit);
}
