package com.storix.domain.domains.topicroom.application.port;

import com.storix.domain.domains.topicroom.dto.PendingChatPush;

import java.util.List;

public interface TopicRoomPushBatchPort {

    boolean tryAcquireSendSlot(Long roomId);

    void accumulate(Long roomId, Long messageId, Long senderId, String senderNickname, String message);

    boolean enqueue(Long roomId);

    List<Long> pollDueRoomIds(int limit);

    PendingChatPush drain(Long roomId);

    Long findLastPushedMessageId(Long roomId);

    void markPushed(Long roomId, Long messageId);
}
