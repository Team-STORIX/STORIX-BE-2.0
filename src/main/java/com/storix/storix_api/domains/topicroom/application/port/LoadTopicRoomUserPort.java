package com.storix.storix_api.domains.topicroom.application.port;

import com.storix.storix_api.domains.topicroom.dto.TopicRoomUserResponseDto;

import java.util.List;
import java.util.Set;

public interface LoadTopicRoomUserPort {
    // 주어진 토픽룸 ID 목록 중 해당 유저가 참여하고 있는 방의 ID 조회
    Set<Long> loadJoinedRoomIds(Long userId, List<Long> roomIds);

    // 특정 유저가 특정 방에 참여 중인지 확인
    boolean existsByUserIdAndRoomId(Long userId, Long roomId);

    // 유저 프로필 조회용
    List<Long> loadMemberIdsByRoomId(Long roomId);
}
