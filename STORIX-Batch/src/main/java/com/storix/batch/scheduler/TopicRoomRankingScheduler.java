package com.storix.batch.scheduler;

import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.topicroom.service.TopicRoomRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicRoomRankingScheduler {

    private static final int MESSAGE_WINDOW_HOURS = 24;
    private static final int FRESHNESS_HOURS = 24;

    private final TopicRoomAdaptor topicRoomAdaptor;
    private final TopicRoomRankingService topicRoomRankingService;

    @CacheEvict(cacheNames = {"trendingLoyaltySlot", "trendingNewUserSlots"},
            allEntries = true, cacheManager = "trendingCacheManager")
    @Scheduled(cron = "0 0 * * * *")
    public void calculatePopularity() {
        log.info(">>>> [Scheduler] 인기도 점수 및 증가율 계산 시작");
        int updated = topicRoomRankingService.calculatePopularity(LocalDateTime.now());
        log.info(">>>> [Scheduler] 총 {}개의 토픽룸 점수 갱신 완료", updated);
    }

    // 24시간마다 previousActiveUserNumber 스냅샷 갱신
    @Scheduled(cron = "0 0 0 * * *")
    public void snapshotActiveUserNumbers() {
        log.info(">>>> [Scheduler] 참여자 수 스냅샷 시작");
        int updated = topicRoomRankingService.snapshotActiveUserNumbers();
        log.info(">>>> [Scheduler] 총 {}개의 토픽룸 스냅샷 완료", updated);
    }

    // HOT 토픽룸 활동 점수 계산
    @Scheduled(cron = "0 0 * * * *")
    public void calculateHotActivityScore() {
        LocalDateTime now = LocalDateTime.now();
        int updated = topicRoomAdaptor.updateActivityScores(
                now.minusHours(MESSAGE_WINDOW_HOURS), now.minusHours(FRESHNESS_HOURS));
        log.info(">>>> [Scheduler] 토픽룸 활동 점수 {}건 갱신 완료", updated);
    }
}
