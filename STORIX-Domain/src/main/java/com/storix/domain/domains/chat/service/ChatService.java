package com.storix.domain.domains.chat.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.chat.application.port.PublishChatPort;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.domain.TopicRoomUser;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomUserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final PublishChatPort publishChatPort;
    private final TopicRoomAdaptor topicRoomAdaptor;
    private final UserAdaptor userAdaptor;
    private final ChatAdaptor chatAdaptor;

    public String validateRoomMemberAndGetNickname(Long userId, Long roomId) {

        // 토픽룸 존재 여부 검증
        if (!topicRoomAdaptor.existsById(roomId)){
            throw UnknownTopicRoomException.EXCEPTION;
        }

        // 해당 토픽룸에 참여 중인 유저인지 검증
        if (!topicRoomAdaptor.existsByUserIdAndRoomId(userId, roomId)) {
            throw UnknownTopicRoomUserException.EXCEPTION;
        }

        User user = userAdaptor.findUserById(userId);

        return user.getDisplayNickName();
    }

    public void validateRoomExistence(Long roomId) {
        if (!topicRoomAdaptor.existsById(roomId)) {
            throw UnknownTopicRoomException.EXCEPTION;
        }
    }

    public void publishRedis(ChatMessage chatMessage, String nickname) {
        publishChatPort.publish(ChatMessageResponseDto.of(chatMessage, nickname));
    }

    @Transactional(readOnly = true)
    public Slice<ChatMessageResponseDto> getChatMessages(Long roomId, List<Long> blockedIds, Pageable pageable) {
        return chatAdaptor.loadMessages(roomId, blockedIds, pageable);
    }

    public LocalDateTime getRoomJoinedAt(Long userId, Long roomId) {
        TopicRoomUser participation = topicRoomAdaptor.findByUserIdAndRoomId(userId, roomId);
        return participation.getCreatedAt();
    }
}
