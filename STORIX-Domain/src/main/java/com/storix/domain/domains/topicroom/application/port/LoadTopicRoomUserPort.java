package com.storix.domain.domains.topicroom.application.port;

import java.util.List;

public interface LoadTopicRoomUserPort {
    // 유저 프로필 조회용
    List<Long> loadMemberIdsByRoomId(Long roomId);
}
