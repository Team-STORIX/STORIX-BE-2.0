package com.storix.batch.scheduler;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.event.adaptor.StoryCardDrawAdaptor;
import com.storix.domain.domains.event.domain.StoryCardDraw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryCardPurgeScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StoryCardDrawAdaptor storyCardDrawAdaptor;

    // 이전 날짜의 뽑기 결과를 삭제한다. (Asia/Seoul 06:00 기준)
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    @Transactional
    public void purgeExpiredDraws() {
        LocalDate cutoff = StoryCardDraw.serviceDateOf(LocalDateTime.now(KST))
                .minusDays(STORIXStatic.StoryCard.RETENTION_DAYS);

        log.info(">>>> [StoryCardPurgeScheduler] 시작 — cutoff: {} (미만 삭제)", cutoff);
        int deleted = storyCardDrawAdaptor.deleteDrawnBefore(cutoff);
        log.info(">>>> [StoryCardPurgeScheduler] 완료 — 삭제: {}건", deleted);
    }
}
