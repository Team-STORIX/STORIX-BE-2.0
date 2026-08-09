package com.storix.batch.scheduler;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;
import com.storix.domain.domains.topicroom.application.port.TopicRoomPushBatchPort;
import com.storix.domain.domains.topicroom.dto.PendingChatPush;
import com.storix.domain.domains.topicroom.service.TopicRoomChatPushService;
import com.storix.infrastructure.external.topicroom.TopicRoomChatPushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomChatPushFlusher {

    private final TopicRoomPushBatchPort topicRoomPushBatchPort;
    private final TopicRoomChatPushSender topicRoomChatPushSender;
    private final TopicRoomChatPushService topicRoomChatPushService;

    @Async("topicRoomPushExecutor")
    public void flush(Long roomId, PendingChatPush pending) {
        try {
            Long upTo = topicRoomChatPushService.findLastMessageId(roomId);
            if (upTo == null) return;

            Long anchor = topicRoomPushBatchPort.findLastPushedMessageId(roomId);
            if (anchor != null && upTo <= anchor) return;

            Long senderId;
            String senderNickname;
            String lastMessage;

            if (pending != null) {
                senderId = pending.lastSenderId();
                senderNickname = pending.lastSenderNickname();
                lastMessage = pending.lastMessage();
            } else {
                ChatMessageResponseDto latest = topicRoomChatPushService.findLatestMessage(roomId);
                if (latest == null) return;
                senderId = latest.senderId();
                senderNickname = latest.senderName();
                lastMessage = latest.message();
            }

            log.info(">>>> [TopicRoomPush] 묶음 flush roomId={}, anchor={}, upTo={}, recovered={}",
                    roomId, anchor, upTo, pending == null);

            topicRoomChatPushSender.send(roomId, anchor, upTo, senderId, senderNickname, lastMessage);
            topicRoomPushBatchPort.markPushed(roomId, upTo);
        } catch (Exception e) {
            topicRoomPushBatchPort.enqueue(roomId);
            log.error(">>>> [TopicRoomPush] flush 실패 roomId={}, cause={}", roomId, e.getMessage(), e);
        }
    }
}
