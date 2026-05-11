package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.UpdateTopicRoomPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TopicRoomPersistenceAdapter implements LoadTopicRoomPort, UpdateTopicRoomPort {

    private final TopicRoomRepository topicRoomRepository;

    @Override
    public boolean existsByWorksId(Long worksId) {
        return topicRoomRepository.existsByWorksId(worksId);
    }

    @Override
    public void updatePopularity(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePopularity(rooms);
    }

    @Override
    public void updatePreviousActiveUserNumbers(List<TopicRoom> rooms) {
        topicRoomRepository.bulkUpdatePreviousActiveUserNumbers(rooms);
    }

    @Override
    public List<TopicRoom> findAllActiveRooms() {
        return topicRoomRepository.findAllActiveRooms();
    }
}