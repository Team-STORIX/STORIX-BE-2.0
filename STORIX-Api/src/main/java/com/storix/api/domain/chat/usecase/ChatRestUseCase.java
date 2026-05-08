package com.storix.api.domain.chat.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.chat.dto.ChatMessageRequestDto;
import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.chat.service.ChatService;
import com.storix.domain.domains.topicroom.service.TopicRoomServiceV2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@UseCase
@RequiredArgsConstructor
public class ChatRestUseCase {

    private final TopicRoomServiceV2 topicRoomService;
    private final ChatService chatService;

    public Slice<ChatMessageResponseDto> getChatHistory(Long roomId, Pageable pageable){

        // 토픽룸 존재 여부 검증
        topicRoomService.findTopicRoomById(roomId);

        // 채팅 메시지 불러오기
        return chatService.getChat(roomId, pageable);
    }
}
