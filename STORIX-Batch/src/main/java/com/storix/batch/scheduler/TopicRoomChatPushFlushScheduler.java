package com.storix.batch.scheduler;

import com.storix.domain.domains.topicroom.application.port.TopicRoomPushBatchPort;
import com.storix.domain.domains.topicroom.dto.RoomLastMessageId;
import com.storix.domain.domains.topicroom.service.TopicRoomChatPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomChatPushFlushScheduler {

    private static final int FLUSH_LIMIT = 100;
    private static final Duration RECOVERY_LOOKBACK = Duration.ofMinutes(5);

    private final TopicRoomPushBatchPort topicRoomPushBatchPort;
    private final TopicRoomChatPushFlusher topicRoomChatPushFlusher;
    private final TopicRoomChatPushService topicRoomChatPushService;

    @Scheduled(fixedDelay = 1000)
    public void flush() {
        List<Long> dueRoomIds = topicRoomPushBatchPort.pollDueRoomIds(FLUSH_LIMIT);
        if (dueRoomIds.isEmpty()) return;

        for (Long roomId : dueRoomIds) {
            try {
                if (!topicRoomPushBatchPort.tryAcquireSendSlot(roomId)) continue;

                // 발송이 1초 틱을 잡지 않도록 방 단위로 떼어낸다. 게이트와 회수는 여기서 끝내둔다
                topicRoomChatPushFlusher.flush(roomId, topicRoomPushBatchPort.drain(roomId));
            } catch (Exception e) {
                log.error(">>>> [TopicRoomPush] flush 실패 roomId={}, cause={}", roomId, e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void recoverMissedRooms() {
        List<Long> activeRoomIds = topicRoomChatPushService.findRecentlyActiveRoomIds(
                LocalDateTime.now().minus(RECOVERY_LOOKBACK));
        if (activeRoomIds.isEmpty()) return;

        Map<Long, Long> lastMessageIds = topicRoomChatPushService.findLastMessageIds(activeRoomIds).stream()
                .collect(Collectors.toMap(RoomLastMessageId::roomId, RoomLastMessageId::messageId));

        int recovered = 0;
        for (Map.Entry<Long, Long> entry : lastMessageIds.entrySet()) {
            Long anchor = topicRoomPushBatchPort.findLastPushedMessageId(entry.getKey());

            // 앵커가 없으면 이미 보냈는지 판단할 수 없어 건드리지 않는다
            if (anchor == null || entry.getValue() <= anchor) continue;

            // 이미 대기열에 있으면 묶음 창에서 정상 대기 중인 것이라 복구가 아니다
            if (topicRoomPushBatchPort.enqueue(entry.getKey())) recovered++;
        }

        if (recovered > 0) {
            log.warn(">>>> [TopicRoomPush] 누락 복구 대기열 등록 rooms={}", recovered);
        }
    }
}
