package com.storix.storix_api.domains.topicroom.application.port;

import com.storix.storix_api.domains.topicroom.domain.TopicRoom;
import com.storix.storix_api.domains.topicroom.domain.TopicRoomReport;
import com.storix.storix_api.domains.topicroom.domain.enums.TopicRoomRole;

import java.time.LocalDateTime;

public interface RecordTopicRoomPort {

    TopicRoom saveRoom(TopicRoom room);

    void saveParticipation(Long userId, TopicRoom room, TopicRoomRole role);

    int deleteParticipation(Long userId, Long roomId);

    void saveReport(TopicRoomReport report);

    void incrementActiveUserNumber(Long roomId);

    void decrementActiveUserNumber(Long roomId);

    void updateLastChatTime(Long roomId, LocalDateTime now);

    void deleteRoom(Long roomId);
}