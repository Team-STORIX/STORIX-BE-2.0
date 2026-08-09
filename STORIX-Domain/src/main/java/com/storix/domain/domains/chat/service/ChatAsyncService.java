package com.storix.domain.domains.chat.service;

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

    private final TopicRoomAdaptor topicRoomAdaptor;

    @Async("chatAsyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // 저장과 발행은 이미 끝난 상태로 들어온다
    public void processAfterMessageSent(ChatMessage savedMessage) {

        // 토픽룸의 마지막 채팅 정보 갱신
        try {
            topicRoomAdaptor.updateLastMessage(
                    savedMessage.getRoomId(),
                    savedMessage.getId(),
                    savedMessage.getMessage(),
                    savedMessage.getMessageType(),
                    savedMessage.getSenderId(),
                    savedMessage.getCreatedAt()
            );
        } catch (Exception e) {

            // 예외 로깅만 하고 상위로 전파되지 않게 함
            log.warn(">>>> [ChatAsyncService] 토픽룸 최신 메시지 업데이트 실패 (RoomID: {}, MsgID: {}): {}",
                    savedMessage.getRoomId(), savedMessage.getId(), e.getMessage());
        }
    }
}
