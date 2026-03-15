package com.storix.batch.scheduler;

import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.UpdateTopicRoomPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicRoomRankingScheduler {

    private final LoadTopicRoomPort loadTopicRoomPort;
    private final UpdateTopicRoomPort updateTopicRoomPort;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void calculatePopularity() {
        log.info(">>>> [Scheduler] 인기도 점수 계산 시작");

        List<TopicRoom> activeRooms = loadTopicRoomPort.findAllActiveRooms();

        if (activeRooms.isEmpty()) { return; }

        LocalDateTime now = LocalDateTime.now();

        for (TopicRoom room : activeRooms) {
            double score = calculateScore(room, now);
            room.updatePopularityScore(score);
        }

        updateTopicRoomPort.updatePopularityScores(activeRooms);
        log.info(">>>> [Scheduler] 총 {}개의 토픽룸 점수 갱신 완료", activeRooms.size());
    }

    private double calculateScore(TopicRoom room, LocalDateTime now) {
        int u = room.getActiveUserNumber();
        LocalDateTime lastChat = room.getLastChatTime() != null ? room.getLastChatTime() : room.getCreatedAt();

        long hours = ChronoUnit.HOURS.between(lastChat, now);
        if (hours < 0) hours = 0;

        return (u * 10.0) / Math.pow((hours + 1), 1.8);
    }
}
