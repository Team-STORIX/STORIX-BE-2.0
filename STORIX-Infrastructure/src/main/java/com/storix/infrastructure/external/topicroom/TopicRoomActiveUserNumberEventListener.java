package com.storix.infrastructure.external.topicroom;

import com.storix.domain.domains.topicroom.dto.TopicRoomActiveUserNumberResponseDto;
import com.storix.domain.domains.topicroom.event.TopicRoomActiveUserNumberChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomActiveUserNumberEventListener {

    private final RedisTopicRoomActiveUserNumberAdapter redisTopicRoomActiveUserNumberAdapter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(TopicRoomActiveUserNumberChangedEvent event) {
        redisTopicRoomActiveUserNumberAdapter.publish(
                new TopicRoomActiveUserNumberResponseDto(event.topicRoomId(), event.activeUserNumber()));
    }
}
