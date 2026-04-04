package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;

import java.util.List;

public interface TopicRoomRankingRepository {

    // 여러 개의 방 인기도 점수와 증가율을 한 번에 업데이트
    void bulkUpdatePopularity(List<TopicRoom> rooms);

    // 여러 개의 방 이전 참여자 수를 한 번에 업데이트
    void bulkUpdatePreviousActiveUserNumbers(List<TopicRoom> rooms);

    // 충성 슬롯: 증가율 기준 상위 1개
    List<TopicRoomResponseDto> findLoyaltySlot();

    // 신규 슬롯: 참여자 수 기준 상위 2개
    List<TopicRoomResponseDto> findNewUserSlots(List<Long> excludeIds, int limit);
}
