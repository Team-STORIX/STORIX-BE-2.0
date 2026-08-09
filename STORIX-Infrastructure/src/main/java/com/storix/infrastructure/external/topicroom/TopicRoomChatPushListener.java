package com.storix.infrastructure.external.topicroom;

import com.storix.domain.domains.topicroom.application.port.TopicRoomPushBatchPort;
import com.storix.domain.domains.topicroom.event.TopicRoomChatPushEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomChatPushListener {

    private final TopicRoomPushBatchPort topicRoomPushBatchPort;
    private final TopicRoomChatPushSender topicRoomChatPushSender;

    @Async("notificationConsumerExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(TopicRoomChatPushEvent event) {
        try {
            if (topicRoomPushBatchPort.tryAcquireSendSlot(event.roomId())) {
                Long anchor = topicRoomPushBatchPort.findLastPushedMessageId(event.roomId());
                log.info(">>>> [TopicRoomPush] 게이트 선점, 즉시 발송 roomId={}, messageId={}, anchor={}",
                        event.roomId(), event.messageId(), anchor);
                topicRoomChatPushSender.send(
                        event.roomId(), anchor, event.messageId(),
                        event.senderId(), event.senderNickname(), event.message());
                topicRoomPushBatchPort.markPushed(event.roomId(), event.messageId());
                return;
            }
            log.debug(">>>> [TopicRoomPush] 게이트 대기, 묶음 누적 roomId={}, messageId={}",
                    event.roomId(), event.messageId());
            topicRoomPushBatchPort.accumulate(
                    event.roomId(), event.messageId(), event.senderId(), event.senderNickname(), event.message());
        } catch (Exception e) {
            log.error(">>>> [TopicRoomPush] 처리 실패 roomId={}, senderId={}, cause={}",
                    event.roomId(), event.senderId(), e.getMessage(), e);
        }
    }
}
