package com.storix.domain.domains.topicroom.application.port;

import com.storix.domain.domains.topicroom.domain.TopicRoom;
import java.util.List;

public interface LoadTopicRoomPort {

    boolean existsByWorksId(Long worksId);

    List<TopicRoom> findAllActiveRooms();
}