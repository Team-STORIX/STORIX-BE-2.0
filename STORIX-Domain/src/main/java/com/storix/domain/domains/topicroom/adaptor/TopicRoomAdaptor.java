package com.storix.domain.domains.topicroom.adaptor;

import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.topicroom.repository.TopicRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TopicRoomAdaptor {

    private final TopicRoomRepository topicRoomRepository;

    public void updateLastMessage(Long roomId, String message, MessageType messageType, Long senderId, LocalDateTime lastChatTime) {
        topicRoomRepository.updateLastMessage(roomId, message, messageType, senderId, lastChatTime);
    }
}
