package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.search.exception.SearchNoTopicRoomFoundException;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.UpdateTopicRoomPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.dto.TopicRoomResponseDto;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TopicRoomPersistenceAdapter implements LoadTopicRoomPort, UpdateTopicRoomPort, LoadTopicRoomUserPort {

    private final TopicRoomRepository topicRoomRepository;
    private final TopicRoomUserRepository topicRoomUserRepository;

    @Override public Slice<TopicRoomResponseDto> searchWithFilters(List<Long> worksIds, Pageable pageable) {
        if (worksIds.isEmpty()) {
            throw SearchNoTopicRoomFoundException.EXCEPTION;
        }

        Slice<TopicRoomResponseDto> result = topicRoomRepository.findBySearchWithFilters(worksIds, pageable);
        if (result.isEmpty()) {
            throw SearchNoTopicRoomFoundException.EXCEPTION;
        }
        return result;
    }

    @Override
    public void updateLastChatTime(Long roomId, LocalDateTime lastChatTime) {
        topicRoomRepository.updateLastChatTime(roomId, lastChatTime);
    }

    @Override
    public boolean existsByWorksId(Long worksId) {
        return topicRoomRepository.existsByWorksId(worksId);
    }

    @Override
    public boolean existsById(Long roomId) {
        return topicRoomRepository.existsById(roomId);
    }

    @Override
    public boolean existsByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.existsByUserIdAndTopicRoomId(userId, roomId);
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