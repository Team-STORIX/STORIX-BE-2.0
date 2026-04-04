package com.storix.batch.scheduler;

import com.storix.domain.domains.topicroom.application.port.LoadTopicRoomPort;
import com.storix.domain.domains.topicroom.application.port.UpdateTopicRoomPort;
import com.storix.domain.domains.topicroom.domain.TopicRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

    @CacheEvict(cacheNames = {"trendingLoyaltySlot", "trendingNewUserSlots"},
            allEntries = true, cacheManager = "trendingCacheManager")
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void calculatePopularity() {
        log.info(">>>> [Scheduler] 인기도 점수 및 증가율 계산 시작");

        List<TopicRoom> activeRooms = loadTopicRoomPort.findAllActiveRooms();

        if (activeRooms.isEmpty()) { return; }

        LocalDateTime now = LocalDateTime.now();

        for (TopicRoom room : activeRooms) {
            room.updatePopularityScore(calculateScore(room, now));
            room.updatePopularityGrowthRate(calculateGrowthRate(room));
        }

        updateTopicRoomPort.updatePopularity(activeRooms);
        log.info(">>>> [Scheduler] 총 {}개의 토픽룸 점수 갱신 완료", activeRooms.size());
    }

    // 24시간마다 previousActiveUserNumber 스냅샷 갱신
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void snapshotActiveUserNumbers() {
        log.info(">>>> [Scheduler] 참여자 수 스냅샷 시작");

        List<TopicRoom> activeRooms = loadTopicRoomPort.findAllActiveRooms();

        if (activeRooms.isEmpty()) { return; }

        for (TopicRoom room : activeRooms) {
            room.snapshotActiveUserNumber();
        }

        updateTopicRoomPort.updatePreviousActiveUserNumbers(activeRooms);
        log.info(">>>> [Scheduler] 총 {}개의 토픽룸 스냅샷 완료", activeRooms.size());
    }

    // 인기도 점수: 기존 공식
    private double calculateScore(TopicRoom room, LocalDateTime now) {
        int u = room.getActiveUserNumber();
        LocalDateTime lastChat = room.getLastChatTime() != null ? room.getLastChatTime() : room.getCreatedAt();

        long hours = ChronoUnit.HOURS.between(lastChat, now);
        if (hours < 0) hours = 0;

        return (u * 10.0) / Math.pow((hours + 1), 1.8);
    }

    // 증가율 (%)
    private double calculateGrowthRate(TopicRoom room) {
        int current = room.getActiveUserNumber();
        int previous = room.getPreviousActiveUserNumber();
        int denominator = Math.max(previous, 1);

        return (double) (current - previous) / denominator * 100;
    }
}
