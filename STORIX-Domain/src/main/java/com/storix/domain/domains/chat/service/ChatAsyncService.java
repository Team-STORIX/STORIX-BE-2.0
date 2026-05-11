package com.storix.domain.domains.chat.service;

import com.storix.domain.domains.chat.adaptor.ChatAdaptor;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAsyncService {

    private final ChatAdaptor chatAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;

    @Async("chatAsyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processAfterMessageSent(ChatMessage chatMessage) {

        // 메시지 저장
        chatAdaptor.saveMessage(chatMessage);

        // 토픽룸의 마지막 채팅 시간 갱신
        try {
            topicRoomAdaptor.updateLastChatTime(chatMessage.getRoomId(), chatMessage.getCreatedAt());
            log.info(">>>> [ChatService] 처리 완료 - SenderID: {}, Content: {}", chatMessage.getSenderId(), chatMessage.getMessage());

        } catch (Exception e) {

            // 예외 로깅만 하고 상위로 전파되지 않게 함
            log.warn(">>>> [ChatAsyncService] 토픽룸 LastChatTime 업데이트 실패 (RoomID: {}, MsgID: {}): {}",
                    chatMessage.getRoomId(), chatMessage.getId(), e.getMessage());
        }
    }
}