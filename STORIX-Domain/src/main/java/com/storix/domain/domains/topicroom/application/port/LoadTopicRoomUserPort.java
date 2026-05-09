package com.storix.domain.domains.topicroom.application.port;

import java.util.List;
import java.util.Set;

public interface LoadTopicRoomUserPort {

    // 특정 유저가 특정 방에 참여 중인지 확인
    boolean existsByUserIdAndRoomId(Long userId, Long roomId);
}
