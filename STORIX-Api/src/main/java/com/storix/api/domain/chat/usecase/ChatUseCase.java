package com.storix.api.domain.chat.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatHistoryResponseDto;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.chat.service.ChatAsyncService;
import com.storix.domain.domains.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ChatUseCase {

    private final ChatService chatService;
    private final ChatAsyncService chatAsyncService;

    @Transactional
    public void sendMessage(Long userId, ChatMessageRequestDto request) {

        log.info(">>>> [ChatService] 메시지 전송 시도 - UserID: {}, RoomID: {}", userId, request.roomId());

        // 토픽룸 유저 권한 관련 검증
        String nickname = chatService.validateRoomMemberAndGetNickname(userId, request.roomId());

        // 채팅 메시지 엔티티 변환
        ChatMessage chatMessage = request.toEntity(userId);

        // Redis 발행
        chatService.publishRedis(chatMessage, nickname);

        // 메시지 전송 후 비동기 처리
        chatAsyncService.processAfterMessageSent(chatMessage);

        log.info(">>>> [ChatService] 처리 완료 - Sender: {}, Content: {}", nickname, chatMessage.getMessage());
    }

    @Transactional(readOnly = true)
    public ChatHistoryResponseDto getChatHistory(Long userId, Long roomId, Pageable pageable) {

        log.info(">>>> [ChatService] 과거 내역 조회 요청 - RoomID: {}, Page: {}", roomId, pageable.getPageNumber());

        // 토픽룸 존재 여부 검증
        chatService.validateRoomExistence(roomId);

        // 채팅방 입장 시점 확인
        LocalDateTime joinedAt = chatService.getRoomJoinedAt(userId, roomId);

        // 과거 메시지 조회
        Slice<ChatMessageResponseDto> chatMessages = chatService.getChatMessages(roomId, pageable);

        return new ChatHistoryResponseDto(joinedAt, chatMessages);
    }
}
