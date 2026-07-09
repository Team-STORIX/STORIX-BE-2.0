package com.storix.batch.scheduler;

import com.storix.domain.domains.feed.service.TodayFeedFeatureService;
import com.storix.domain.domains.topicroom.service.HotTopicRoomFeatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeaturedContentNotificationScheduler {

    private final TodayFeedFeatureService todayFeedFeatureService;
    private final HotTopicRoomFeatureService hotTopicRoomFeatureService;

    // 피드 랭킹 20분 갱신 직후 실행
    @Scheduled(cron = "0 5/20 * * * *", zone = "Asia/Seoul")
    public void notifyTodayFeed() {
        try {
            todayFeedFeatureService.selectAndNotify();
        } catch (Exception e) {
            log.error(">>> [FeaturedContentNotificationScheduler] 오늘의 피드 선정 알림 실패 cause={}", e.getMessage(), e);
        }
    }

    // 토픽룸 활동 점수 매시 갱신 직후 실행
    @Scheduled(cron = "0 5 * * * *", zone = "Asia/Seoul")
    public void notifyHotTopicRoom() {
        try {
            hotTopicRoomFeatureService.selectAndNotify();
        } catch (Exception e) {
            log.error(">>> [FeaturedContentNotificationScheduler] HOT 토픽룸 선정 알림 실패 cause={}", e.getMessage(), e);
        }
    }
}
