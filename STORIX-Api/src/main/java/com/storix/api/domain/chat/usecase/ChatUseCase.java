package com.storix.api.domain.chat.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.chat.service.ChatAsyncService;
import com.storix.domain.domains.chat.service.ChatService;
import com.storix.domain.domains.topicroom.service.TopicRoomService;
import com.storix.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ChatUseCase {

    private final ChatService chatService;
    private final ChatAsyncService chatAsyncService;
    private final TopicRoomService topicRoomService;
    private final UserService userService;

    public void sendMessage(Long userId, ChatMessageRequestDto request) {

        // 토픽룸 존재 여부 및 참여자 검증
        topicRoomService.validateRoomMember(userId, request.roomId());

        // 발신자 닉네임 조회
        String nickname = userService.findUserById(userId).getNickName();

        // 채팅 메시지 생성
        ChatMessage chatMessage = chatService.createMessage(userId, request);

        // Redis 채널로 실시간 메시지 발행
        chatService.publishMessage(nickname, chatMessage);

        // 메시지 저장 및 토픽룸 마지막 채팅 시간 갱신
        chatAsyncService.processAfterMessageSent(chatMessage);
    }
}
