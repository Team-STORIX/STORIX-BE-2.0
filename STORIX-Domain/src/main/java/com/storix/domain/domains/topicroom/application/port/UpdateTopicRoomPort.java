package com.storix.domain.domains.topicroom.application.port;

import com.storix.domain.domains.topicroom.domain.TopicRoom;

import java.time.LocalDateTime;
import java.util.List;

public interface UpdateTopicRoomPort {

    // 여러 방의 인기도 점수 일괄 업데이트
    void updatePopularity(List<TopicRoom> rooms);

    // 여러 방의 이전 참여자 수 일괄 업데이트
    void updatePreviousActiveUserNumbers(List<TopicRoom> rooms);
}
