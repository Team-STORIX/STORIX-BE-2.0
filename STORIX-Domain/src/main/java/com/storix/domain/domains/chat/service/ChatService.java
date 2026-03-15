package com.storix.domain.domains.chat.service;

import com.storix.domain.domains.chat.application.port.LoadChatPort;
import com.storix.domain.domains.chat.application.port.PublishChatPort;
import com.storix.domain.domains.chat.application.usecase.ChatUseCase;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomUserPort;
import com.storix.domain.domains.user.application.port.LoadUserPort;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomException;
import com.storix.domain.domains.topicroom.exception.UnknownTopicRoomUserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private final PublishChatPort publishChatPort;
    private final LoadUserPort loadUserPort;
    private final LoadTopicRoomPort loadTopicRoomPort;
    private final LoadChatPort loadChatPort;
    private final ChatAsyncService chatAsyncService;
    private final LoadTopicRoomUserPort loadTopicRoomUserPort;

    @Override
    @Transactional
    public void sendMessage(Long userId, ChatMessageRequestDto request) {


        log.info(">>>> [ChatService] 메시지 전송 시도 - UserID: {}, RoomID: {}", userId, request.roomId());

        // 토픽룸 존재 여부 검증
        if (!loadTopicRoomPort.existsById(request.roomId())) {
            throw UnknownTopicRoomException.EXCEPTION;
        }

        // 해당 토픽룸에 참여 중인 유저인지 검증
        if (!loadTopicRoomUserPort.existsByUserIdAndRoomId(userId, request.roomId())) {
            throw UnknownTopicRoomUserException.EXCEPTION;
        }

        User user = loadUserPort.findById(userId);
        String nickname = user.getNickName();

        ChatMessage chatMessage = request.toEntity(userId);

        // Redis 발행
        publishChatPort.publish(ChatMessageResponseDto.of(chatMessage, nickname));

        chatAsyncService.processAfterMessageSent(chatMessage);

        log.info(">>>> [ChatService] 처리 완료 - Sender: {}, Content: {}", nickname, chatMessage.getMessage());
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<ChatMessageResponseDto> getChatHistory(Long roomId,  Pageable pageable) {

        log.info(">>>> [ChatService] 과거 내역 조회 요청 - RoomID: {}, Page: {}", roomId, pageable.getPageNumber());

        // 토픽룸 존재 여부 검증
        if (!loadTopicRoomPort.existsById(roomId)) {
            throw UnknownTopicRoomException.EXCEPTION;
        }

        return loadChatPort.loadMessages(roomId, pageable);
    }
}