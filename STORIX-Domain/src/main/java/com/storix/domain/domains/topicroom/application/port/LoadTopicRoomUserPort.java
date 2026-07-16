package com.storix.domain.domains.topicroom.application.port;

import java.util.List;
import java.util.Map;

public interface LoadTopicRoomUserPort {
    // 유저 프로필 조회용
    List<Long> loadMemberIdsByRoomId(Long roomId);

    // 여러 방의 멤버를 한 번에 조회
    Map<Long, List<Long>> loadMembersByRoomIds(List<Long> roomIds);
}
