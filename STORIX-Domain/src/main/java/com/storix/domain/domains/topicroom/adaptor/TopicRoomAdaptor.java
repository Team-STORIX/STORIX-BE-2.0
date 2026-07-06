package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomUserException;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import com.storix.domain.domains.topicroom.repository.TopicRoomUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TopicRoomAdaptor {

    private final TopicRoomRepository topicRoomRepository;
    private final TopicRoomUserRepository topicRoomUserRepository;

    public void updateLastMessage(Long roomId, String message, MessageType messageType, Long senderId, LocalDateTime lastChatTime) {
        topicRoomRepository.updateLastMessage(roomId, message, messageType, senderId, lastChatTime);
    }

    public boolean existsById(Long roomId) {
        return topicRoomRepository.existsById(roomId);
    }

    public boolean existsByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.existsByUserIdAndTopicRoomId(userId, roomId);
    }

    public List<TopicRoom> loadTop5PopularRooms() {
        return topicRoomRepository.findTop5ByOrderByPopularityScoreDescLastChatTimeDesc();
    }

    // 주어진 토픽룸 ID 목록 중 해당 유저가 참여하고 있는 방의 ID 조회
    public Set<Long> loadJoinedRoomIds(Long userId, List<Long> roomIds) {

        // 빈 리스트일 경우 -> 빈 Set 반환
        if (roomIds == null || roomIds.isEmpty()) {
            return Collections.emptySet();
        }

        return topicRoomUserRepository.findJoinedRoomIdsByUserIdAndRoomIds(userId, roomIds);
    }

    public TopicRoomUser findByUserIdAndRoomId(Long userId, Long roomId) {
        return topicRoomUserRepository.findByUserIdAndTopicRoomId(userId, roomId)
                .orElseThrow(() -> UnknownTopicRoomUserException.EXCEPTION);
    }

    public long countJoinedRooms(Long userId) {
        return topicRoomUserRepository.countByUserId(userId);
    }

    public Integer findActiveUserNumberById(Long roomId) {
        Integer activeUserNumber = topicRoomRepository.findActiveUserNumberById(roomId);
        if (activeUserNumber == null) {
            throw UnknownTopicRoomException.EXCEPTION;
        }
        return activeUserNumber;
    }
}
