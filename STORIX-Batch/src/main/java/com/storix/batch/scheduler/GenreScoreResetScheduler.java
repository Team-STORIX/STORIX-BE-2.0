package com.storix.batch.scheduler;

import com.storix.domain.domains.genrescore.service.GenreScoreAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreScoreResetScheduler {

    private final GenreScoreAggregationService aggregationService;

    // 유저 장르 점수 주기별 초기화
    @Scheduled(cron = "0 0 4 1 1 *", zone = "Asia/Seoul")
    public void resetAll() {
        long started = System.currentTimeMillis();
        log.info(">>> [GenreScoreReset] start");
        aggregationService.resetAllRawScores();
        log.info(">>> [GenreScoreReset] done. took={}ms", System.currentTimeMillis() - started);
    }
}
