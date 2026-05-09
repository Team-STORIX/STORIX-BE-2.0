package com.storix.domain.domains.topicroom.application.port;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomReport;
import com.storix.domain.domains.topicroom.domain.enums.TopicRoomRole;

import java.time.LocalDateTime;

public interface RecordTopicRoomPort {

    void saveReport(TopicRoomReport report);
}