package com.storix.domain.domains.chat.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.chat.application.ChatMessagePublisher;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService  {

    private final ChatAdaptor chatAdaptor;
    private final ChatMessagePublisher chatMessagePublisher;

    public ChatMessage createMessage(Long userId, ChatMessageRequestDto request) {
        ChatMessage chatMessage = request.toEntity(userId);
        return chatMessage;
    }

    public void publishMessage(String nickname, ChatMessage chatMessage) {

        log.info(">>>> [ChatService] 메시지 전송 시도 - UserID: {}, RoomID: {}", chatMessage.getSenderId(), chatMessage.getRoomId());

        chatMessagePublisher.publish(ChatMessageResponseDto.of(chatMessage, nickname));

    }

    @Transactional(readOnly = true)
    public Slice<ChatMessageResponseDto> getChat(Long roomId,  Pageable pageable) {

        log.info(">>>> [ChatService] 과거 내역 조회 요청 - RoomID: {}, Page: {}", roomId, pageable.getPageNumber());

        return chatAdaptor.loadMessages(roomId, pageable);
    }
}