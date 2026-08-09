package com.storix.infrastructure.external.topicroom;

import com.storix.domain.domains.topicroom.service.TopicRoomUnreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomReadMarker {

    private final TopicRoomUnreadService topicRoomUnreadService;

    @Async("chatAsyncExecutor")
    public void markRead(Long userId, Long roomId) {
        try {
            topicRoomUnreadService.markRoomRead(userId, roomId);
        } catch (Exception e) {
            log.warn(">>>> [TopicRoomRead] 읽음 처리 실패 userId={}, roomId={}, cause={}",
                    userId, roomId, e.getMessage());
        }
    }
}
