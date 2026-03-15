package com.storix.domain.domains.topicroom.repository;

import com.storix.domain.domains.topicroom.domain.TopicRoom;

import java.util.List;

public interface TopicRoomRankingRepository {

    // 여러 개의 방 점수를 한 번에 업데이트
    void bulkUpdatePopularityScores(List<TopicRoom> rooms);
}
