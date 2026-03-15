package com.storix.storix_api.domains.topicroom.application.port;

import com.storix.storix_api.domains.topicroom.domain.TopicRoom;

import java.time.LocalDateTime;
import java.util.List;

public interface UpdateTopicRoomPort {

    void updateLastChatTime(Long roomId, LocalDateTime lastChatTime);

    // 여러 방의 인기도 점수 일괄 업데이트
    void updatePopularityScores(List<TopicRoom> rooms);
}
